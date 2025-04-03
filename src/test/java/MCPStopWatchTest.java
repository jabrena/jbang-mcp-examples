import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MCPStopWatchTest {

    @Mock
    private MCPStopWatch.StopWatch mockStopWatch;
    private MCPStopWatch stopWatch;

    @BeforeEach
    void setUp() {
        stopWatch = new MCPStopWatch(mockStopWatch);
    }

    @Test
    void shouldReturnStartMessageWhenStopwatchIsStarted() {
        // Given
        when(mockStopWatch.start()).thenReturn("Stopwatch started");

        // When
        String result = stopWatch.start();

        // Then
        assertThat(result).isEqualTo("Stopwatch started");
    }
    
    @Test
    void shouldReturnElapsedTimeWhenStopwatchIsStopped() {
        // Given
        when(mockStopWatch.stop()).thenReturn("Elapsed time: 1000 milliseconds");
        
        // When
        String result = stopWatch.stop();
        
        // Then
        assertThat(result).isEqualTo("Elapsed time: 1000 milliseconds");
    }
} 