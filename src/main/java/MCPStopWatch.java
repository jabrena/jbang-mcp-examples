///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.quarkus:quarkus-bom:3.21.1@pom
//DEPS io.quarkiverse.mcp:quarkus-mcp-server-stdio:1.0.0
//FILES application.properties

import io.quarkiverse.mcp.server.Tool;

public class MCPStopWatch {

    private StopWatch stopWatch;

    public MCPStopWatch() {
        stopWatch = new StopWatch();
    }
    
    public MCPStopWatch(StopWatch stopWatch) {
        this.stopWatch = stopWatch;
    }

    @Tool(description = "Starts the stopwatch")
    public String start() {
        return stopWatch.start();
    }

    @Tool(description = "Stops the stopwatch")
    public String stop() {
        return stopWatch.stop();
    }

    static class StopWatch {
        private long startTime;
        private long endTime;

        public long getStartTime() {
            return startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public String start() { 
            startTime = System.currentTimeMillis();
            return "Stopwatch started";
        }

        public String stop() {
            endTime = System.currentTimeMillis();
            return getElapsedTime();
        }

        private String getElapsedTime() {
            return "Elapsed time: " + (endTime - startTime) + " milliseconds";
        }
    }
}
