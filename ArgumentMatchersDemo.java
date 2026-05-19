import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

// Assume this is the interface we want to mock
interface DataService {
    void saveData(String id, byte[] data);
    String processRequest(int code, String message);
    boolean sendNotification(String recipient, String subject);
}

// A simple class that uses the DataService
class ServiceUnderTest {
    private DataService dataService;

    public ServiceUnderTest(DataService dataService) {
        this.dataService = dataService;
    }

    public void performAction(String userId, byte[] payload) {
        // In a real scenario, the ID might be generated or dynamic
        // We want to ensure saveData is called with *some* ID and the payload
        dataService.saveData(userId, payload);
    }

    public String handleRequest(int type, String input) {
        // We might only care that processRequest is called with a positive code
        // and any string message, and we want to mock its return value
        return dataService.processRequest(type, input);
    }

    public void sendAlert(String email) {
        // We want to verify that sendNotification is called with a specific email
        // and *any* subject line that contains "Alert"
        dataService.sendNotification(email, "Urgent Alert: System Issue");
    }
}

public class ArgumentMatchersDemo {

    @Test
    void testPerformActionWithAnyString() {
        // Create a mock of DataService
        DataService mockDataService = mock(DataService.class);
        ServiceUnderTest service = new ServiceUnderTest(mockDataService);

        byte[] dummyPayload = {1, 2, 3};
        String userId = "user123";

        // When saveData is called with ANY String for the ID and the specific dummyPayload,
        // do nothing (void method)
        // This is where Argument Matchers shine: we don't need to know the exact ID
        when(mockDataService.saveData(anyString(), eq(dummyPayload))).thenAnswer(invocation -> {
            System.out.println("saveData called with ID: " + invocation.getArgument(0) + " and payload length: " + ((byte[])invocation.getArgument(1)).length);
            return null; // void method
        });

        // Call the method under test
        service.performAction(userId, dummyPayload);

        // Verify that saveData was called with the expected payload and *any* string ID
        verify(mockDataService).saveData(anyString(), eq(dummyPayload));
    }

    @Test
    void testHandleRequestWithAnyIntAndString() {
        DataService mockDataService = mock(DataService.class);
        ServiceUnderTest service = new ServiceUnderTest(mockDataService);

        // When processRequest is called with any integer greater than 0
        // and any string, return a specific mocked response.
        // This demonstrates matching based on conditions.
        when(mockDataService.processRequest(gt(0), anyString())).thenReturn("Processed Successfully");

        // Call the method under test with specific values
        String result = service.handleRequest(100, "Some request data");

        // Assert the returned value from the mocked method
        assertEquals("Processed Successfully", result);

        // Verify that processRequest was called with arguments matching our conditions
        verify(mockDataService).processRequest(gt(0), anyString());
    }

    @Test
    void testSendAlertWithSpecificRecipientAndMatchingSubject() {
        DataService mockDataService = mock(DataService.class);
        ServiceUnderTest service = new ServiceUnderTest(mockDataService);

        String recipientEmail = "admin@example.com";

        // We want to ensure sendNotification is called with a specific recipient
        // and a subject that *contains* the word "Alert".
        // contains() is another useful argument matcher.
        when(mockDataService.sendNotification(eq(recipientEmail), contains("Alert"))).thenAnswer(invocation -> {
            System.out.println("Notification sent to: " + invocation.getArgument(0) + " with subject: " + invocation.getArgument(1));
            return true;
        });

        // Call the method under test
        service.sendAlert(recipientEmail);

        // Verify that sendNotification was called with the specific email
        // and a subject that contains "Alert"
        verify(mockDataService).sendNotification(eq(recipientEmail), contains("Alert"));
    }
}