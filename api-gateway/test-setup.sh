#!/bin/bash

echo "Testing API Gateway JWT Authentication Setup"
echo "============================================"

# Test health endpoint (should work without authentication)
echo "1. Testing health endpoint (public):"
curl -s http://localhost:8080/health
echo -e "\n"

# Test public test endpoint (should work without authentication)
echo "2. Testing public test endpoint:"
curl -s http://localhost:8080/test/public
echo -e "\n"

# Test secure endpoint without token (should fail)
echo "3. Testing secure endpoint without token (should fail):"
curl -s -w "HTTP Status: %{http_code}\n" http://localhost:8080/test/secure
echo -e "\n"

# Test secure endpoint with invalid token (should fail)
echo "4. Testing secure endpoint with invalid token (should fail):"
curl -s -w "HTTP Status: %{http_code}\n" -H "Authorization: Bearer invalid-token" http://localhost:8080/test/secure
echo -e "\n"

echo "Setup test completed!"
echo "To test with a valid JWT token:"
echo "1. First login to get a token:"
echo "   curl -X POST http://localhost:8080/auth/login -H 'Content-Type: application/json' -d '{\"email\":\"user@example.com\",\"password\":\"password\"}'"
echo ""
echo "2. Then use the token to access secure endpoints:"
echo "   curl -H 'Authorization: Bearer <your-token>' http://localhost:8080/test/secure"
