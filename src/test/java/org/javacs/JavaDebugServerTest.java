package org.javacs;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;
import org.javacs.debug.*;
import org.javacs.debug.proto.*;
import org.junit.Test;

public class JavaDebugServerTest {
    Path workingDirectory = Paths.get("src/test/examples/debug");
    DebugClient client = new MockClient();
    JavaDebugServer server = new JavaDebugServer(client);
    Process process;
    ArrayBlockingQueue<StoppedEventBody> stoppedEvents = new ArrayBlockingQueue<>(10);

    class MockClient implements DebugClient {
        @Override
        public void initialized() {}

        @Override
        public void stopped(StoppedEventBody evt) {
            stoppedEvents.add(evt);
        }

        @Override
        public void terminated(TerminatedEventBody evt) {}

        @Override
        public void exited(ExitedEventBody evt) {}

        @Override
        public void output(OutputEventBody evt) {
            LOG.info(evt.output);
        }

        @Override
        public void breakpoint(BreakpointEventBody evt) {
            if (evt.breakpoint.verified) {
                LOG.info(
                        String.format(
                                "Breakpoint at %s:%d is verified", evt.breakpoint.source.path, evt.breakpoint.line));
            } else {
                LOG.info(
                        String.format(
                                "Breakpoint at %s:%d cannot be verified because %s",
                                evt.breakpoint.source.path, evt.breakpoint.line, evt.breakpoint.message));
            }
        }

        @Override
        public RunInTerminalResponseBody runInTerminal(RunInTerminalRequest req) {
            throw new UnsupportedOperationException();
        }
    }

    public void launchProcess(String mainClass) throws IOException, InterruptedException {
        var command =
                List.of("java", "-Xdebug", "-Xrunjdwp:transport=dt_socket,address=5005,server=y,suspend=y", mainClass);
        LOG.info("Launch " + String.join(", ", command));
        process = new ProcessBuilder().command(command).directory(workingDirectory.toFile()).inheritIO().start();
        java.lang.Thread.sleep(1000);
    }

    private void attach(int port) {
        var attach = new AttachRequestArguments();
        attach.port = port;
        attach.sourceRoots = new String[] {};
        server.attach(attach);
    }

    private void setBreakpoint(String className, int line) {
        var set = new SetBreakpointsArguments();
        var point = new SourceBreakpoint();
        point.line = line;
        set.source.path = workingDirectory.resolve(className + ".java").toString();
        set.breakpoints = new SourceBreakpoint[] {point};
        server.setBreakpoints(set);
    }

    @Test
    public void attachToProcess() throws IOException, InterruptedException {
        launchProcess("Hello");
        attach(5005);
        server.configurationDone();
        process.waitFor();
    }

    @Test
    public void setBreakpoint() throws IOException, InterruptedException {
        launchProcess("Hello");
        // Attach to the process
        attach(5005);
        // Set a breakpoint at Hello.java:4
        setBreakpoint("Hello", 4);
        server.configurationDone();
        // Wait for stop
        stoppedEvents.take();
        // Find the main thread
        var threads = server.threads().threads;
        for (var t : threads) {
            if (t.name.equals("main")) {
                // Get the stack trace
                var requestTrace = new StackTraceArguments();
                requestTrace.threadId = t.id;
                var stack = server.stackTrace(requestTrace);
                System.out.println("Thread main:");
                for (var frame : stack.stackFrames) {
                    System.out.println(String.format("\t%s:%d (%s)", frame.name, frame.line, frame.source.path));
                }
                // Get variables
                var requestScopes = new ScopesArguments();
                requestScopes.frameId = stack.stackFrames[0].id;
                var scopes = server.scopes(requestScopes).scopes;
                // Get locals
                var requestLocals = new VariablesArguments();
                requestLocals.variablesReference = scopes[0].variablesReference;
                var locals = server.variables(requestLocals).variables;
                System.out.println("Locals:");
                for (var v : locals) {
                    System.out.println(String.format("\t%s %s = %s", v.type, v.name, v.value));
                }
                // Get arguments
                var requestArgs = new VariablesArguments();
                requestArgs.variablesReference = scopes[1].variablesReference;
                var arguments = server.variables(requestArgs).variables;
                System.out.println("Arguments:");
                for (var v : arguments) {
                    System.out.println(String.format("\t%s %s = %s", v.type, v.name, v.value));
                }
            }
        }
        // Wait for process to exit
        server.continue_(new ContinueArguments());
        process.waitFor();
    }

    @Test
    public void step() throws IOException, InterruptedException {
        launchProcess("Hello");
        // Attach to the process
        attach(5005);
        // Set a breakpoint at HelloWorld.java:4
        setBreakpoint("Hello", 4);
        server.configurationDone();
        // Wait for stop
        stoppedEvents.take();
        // Find the main thread
        var threads = server.threads().threads;
        for (var t : threads) {
            if (t.name.equals("main")) {
                var next = new NextArguments();
                next.threadId = t.id;
                server.next(next);
                // Wait for stop
                stoppedEvents.take();
            }
        }
        // Wait for process to exit
        server.continue_(new ContinueArguments());
        process.waitFor();
    }

    @Test
    public void addBreakpoint() throws IOException, InterruptedException {
        launchProcess("Hello");
        // Attach to the process
        attach(5005);
        // Stop at 4
        setBreakpoint("Hello", 4);
        server.configurationDone();
        stoppedEvents.take();
        // Stop at 6
        setBreakpoint("Hello", 6);
        server.continue_(new ContinueArguments());
        stoppedEvents.take();
        // Wait for process to exit
        server.continue_(new ContinueArguments());
        process.waitFor();
    }

    @Test
    public void printCollections() throws IOException, InterruptedException {
        launchProcess("Collections");
        attach(5005);
        setBreakpoint("Collections", 8);
        server.configurationDone();
        stoppedEvents.take();
        // Find the main thread
        var threads = server.threads().threads;
        for (var t : threads) {
            if (t.name.equals("main")) {
                // Get the stack trace
                var requestTrace = new StackTraceArguments();
                requestTrace.threadId = t.id;
                var stack = server.stackTrace(requestTrace);
                System.out.println("Thread main:");
                for (var frame : stack.stackFrames) {
                    System.out.println(String.format("\t%s:%d (%s)", frame.name, frame.line, frame.source.path));
                }
                // Get variables
                var requestScopes = new ScopesArguments();
                requestScopes.frameId = stack.stackFrames[0].id;
                var scopes = server.scopes(requestScopes).scopes;
                // Get locals
                var requestLocals = new VariablesArguments();
                requestLocals.variablesReference = scopes[0].variablesReference;
                var locals = server.variables(requestLocals).variables;
                System.out.println("Locals:");
                for (var v : locals) {
                    System.out.println(String.format("\t%s %s = %s", v.type, v.name, v.value));
                }
            }
        }
        // Wait for process to exit
        server.continue_(new ContinueArguments());
        process.waitFor();
    }

    @Test
    public void arrayVariableShowsIndexedChildren() throws IOException, InterruptedException {
        launchProcess("Arrays");
        attach(5005);
        setBreakpoint("Arrays", 4);
        server.configurationDone();
        stoppedEvents.take();
        // Find the main thread
        var threads = server.threads().threads;
        org.javacs.debug.proto.Variable arrVar = null;
        for (var t : threads) {
            if (t.name.equals("main")) {
                var requestTrace = new StackTraceArguments();
                requestTrace.threadId = t.id;
                var stack = server.stackTrace(requestTrace);
                var requestScopes = new ScopesArguments();
                requestScopes.frameId = stack.stackFrames[0].id;
                var scopes = server.scopes(requestScopes).scopes;
                var requestLocals = new VariablesArguments();
                requestLocals.variablesReference = scopes[0].variablesReference;
                var locals = server.variables(requestLocals).variables;
                for (var v : locals) {
                    if (v.name.equals("arr")) {
                        arrVar = v;
                    }
                }
            }
        }
        // arr must be found
        org.junit.Assert.assertNotNull("arr variable not found", arrVar);
        // value must be "int[3]"
        org.junit.Assert.assertEquals("int[3]", arrVar.value);
        // must have a variablesReference so children can be fetched
        org.junit.Assert.assertTrue("variablesReference must be > 0", arrVar.variablesReference > 0);
        // fetch children
        var requestChildren = new VariablesArguments();
        requestChildren.variablesReference = arrVar.variablesReference;
        var children = server.variables(requestChildren).variables;
        org.junit.Assert.assertEquals(3, children.length);
        org.junit.Assert.assertEquals("[0]", children[0].name);
        org.junit.Assert.assertEquals("1", children[0].value);
        org.junit.Assert.assertEquals("[1]", children[1].name);
        org.junit.Assert.assertEquals("2", children[1].value);
        org.junit.Assert.assertEquals("[2]", children[2].name);
        org.junit.Assert.assertEquals("3", children[2].value);
        // Wait for process to exit
        server.continue_(new ContinueArguments());
        process.waitFor();
    }


    @Test
    public void emptyArrayShowsNoChildren() throws IOException, InterruptedException {
        launchProcess("ArraysEmpty");
        attach(5005);
        setBreakpoint("ArraysEmpty", 4);
        server.configurationDone();
        stoppedEvents.take();
        var threads = server.threads().threads;
        org.javacs.debug.proto.Variable arrVar = null;
        for (var t : threads) {
            if (t.name.equals("main")) {
                var requestTrace = new StackTraceArguments();
                requestTrace.threadId = t.id;
                var stack = server.stackTrace(requestTrace);
                var requestScopes = new ScopesArguments();
                requestScopes.frameId = stack.stackFrames[0].id;
                var scopes = server.scopes(requestScopes).scopes;
                var requestLocals = new VariablesArguments();
                requestLocals.variablesReference = scopes[0].variablesReference;
                var locals = server.variables(requestLocals).variables;
                for (var v : locals) {
                    if (v.name.equals("arr")) arrVar = v;
                }
            }
        }
        org.junit.Assert.assertNotNull("arr variable not found", arrVar);
        org.junit.Assert.assertEquals("int[0]", arrVar.value);
        org.junit.Assert.assertTrue("variablesReference must be > 0", arrVar.variablesReference > 0);
        var requestChildren = new VariablesArguments();
        requestChildren.variablesReference = arrVar.variablesReference;
        var children = server.variables(requestChildren).variables;
        org.junit.Assert.assertEquals(0, children.length);
        server.continue_(new ContinueArguments());
        process.waitFor();
    }

    @Test
    public void nullElementsInArrayShowNullString() throws IOException, InterruptedException {
        launchProcess("ArraysWithNull");
        attach(5005);
        setBreakpoint("ArraysWithNull", 4);
        server.configurationDone();
        stoppedEvents.take();
        var threads = server.threads().threads;
        org.javacs.debug.proto.Variable arrVar = null;
        for (var t : threads) {
            if (t.name.equals("main")) {
                var requestTrace = new StackTraceArguments();
                requestTrace.threadId = t.id;
                var stack = server.stackTrace(requestTrace);
                var requestScopes = new ScopesArguments();
                requestScopes.frameId = stack.stackFrames[0].id;
                var scopes = server.scopes(requestScopes).scopes;
                var requestLocals = new VariablesArguments();
                requestLocals.variablesReference = scopes[0].variablesReference;
                var locals = server.variables(requestLocals).variables;
                for (var v : locals) {
                    if (v.name.equals("arr")) arrVar = v;
                }
            }
        }
        org.junit.Assert.assertNotNull("arr variable not found", arrVar);
        org.junit.Assert.assertEquals("java.lang.String[3]", arrVar.value);
        var requestChildren = new VariablesArguments();
        requestChildren.variablesReference = arrVar.variablesReference;
        var children = server.variables(requestChildren).variables;
        org.junit.Assert.assertEquals(3, children.length);
        org.junit.Assert.assertEquals("null", children[0].value);
        org.junit.Assert.assertEquals("\"hello\"", children[1].value);
        org.junit.Assert.assertEquals("null", children[2].value);
        server.continue_(new ContinueArguments());
        process.waitFor();
    }

    @Test
    public void objectVariableShowsInstanceFields() throws IOException, InterruptedException {
        launchProcess("ObjectFields");
        attach(5005);
        setBreakpoint("ObjectFields", 9);
        server.configurationDone();
        stoppedEvents.take();
        var threads = server.threads().threads;
        org.javacs.debug.proto.Variable pointVar = null;
        for (var t : threads) {
            if (t.name.equals("main")) {
                var requestTrace = new StackTraceArguments();
                requestTrace.threadId = t.id;
                var stack = server.stackTrace(requestTrace);
                var requestScopes = new ScopesArguments();
                requestScopes.frameId = stack.stackFrames[0].id;
                var scopes = server.scopes(requestScopes).scopes;
                var requestLocals = new VariablesArguments();
                requestLocals.variablesReference = scopes[0].variablesReference;
                var locals = server.variables(requestLocals).variables;
                for (var v : locals) {
                    if (v.name.equals("p")) {
                        pointVar = v;
                    }
                }
            }
        }
        org.junit.Assert.assertNotNull("variable p not found", pointVar);
        org.junit.Assert.assertTrue("p must have variablesReference > 0", pointVar.variablesReference > 0);
        var requestFields = new VariablesArguments();
        requestFields.variablesReference = pointVar.variablesReference;
        var fields = server.variables(requestFields).variables;
        org.junit.Assert.assertTrue("p must have at least 2 fields", fields.length >= 2);
        var xField = java.util.Arrays.stream(fields).filter(f -> f.name.equals("x")).findFirst().orElse(null);
        var yField = java.util.Arrays.stream(fields).filter(f -> f.name.equals("y")).findFirst().orElse(null);
        org.junit.Assert.assertNotNull("field x not found", xField);
        org.junit.Assert.assertNotNull("field y not found", yField);
        org.junit.Assert.assertEquals("3", xField.value);
        org.junit.Assert.assertEquals("7", yField.value);
        server.continue_(new ContinueArguments());
        process.waitFor();
    }

    private static final Logger LOG = Logger.getLogger("main");
}
