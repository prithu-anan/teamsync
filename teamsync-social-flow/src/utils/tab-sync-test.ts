// Test utility for cross-tab message synchronization
// This can be used in browser console to test the BroadcastChannel functionality

export const testTabSync = () => {
  if (typeof BroadcastChannel === 'undefined') {
    console.error('BroadcastChannel is not supported in this browser');
    return;
  }

  const testChannel = new BroadcastChannel('teamsync-test');
  
  console.log('Testing cross-tab synchronization...');
  
  // Listen for test messages
  testChannel.onmessage = (event) => {
    console.log('Received test message:', event.data);
  };
  
  // Send a test message
  testChannel.postMessage({
    type: 'TEST',
    data: { message: 'Hello from tab sync test!', timestamp: Date.now() },
    timestamp: Date.now(),
    tabId: 'test-tab'
  });
  
  console.log('Test message sent. Check other tabs for reception.');
  
  // Clean up after 5 seconds
  setTimeout(() => {
    testChannel.close();
    console.log('Test channel closed.');
  }, 5000);
};

// Make it available globally for console testing
if (typeof window !== 'undefined') {
  (window as any).testTabSync = testTabSync;
}
