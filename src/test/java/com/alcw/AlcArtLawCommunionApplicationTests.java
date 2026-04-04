package com.alcw;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.data.mongodb.uri=mongodb://localhost:27017/",
		"spring.data.mongodb.database=alc_test",
		"cloudinary.cloud-name=test-cloud",
		"cloudinary.api-key=test-key",
		"cloudinary.api-secret=test-secret",
		"brevo.api.key=test-brevo-key",
		"app.admin.email=test-admin@example.com",
		"brevo.sender.email=test@example.com",
		"brevo.sender.name=Test Sender",
		"jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
		"jwt.expiration=900000",
		"jwt.issuer=test-issuer",
		"google.sheets.id=test-sheet-id",
		"google.sheets.range=Sheet1!A1:B2",
		"google.credentials=test-credentials.json",
		"app.security.cors.allowed-origins=http://localhost:5173",
		"app.security.rate-limit.max-requests-per-minute=30"
})
class AlcArtLawCommunionApplicationTests {

	@Test
	void contextLoads() {
	}

}
