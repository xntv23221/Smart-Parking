async function test() {
  try {
    // 1. Login
    console.log("Logging in...");
    const loginRes = await fetch('http://localhost:8080/api/public/v1/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: 'client', password: 'client123' })
    });
    
    const loginData = await loginRes.json();
    
    if (loginData.code !== 0) {
      console.error("Login failed:", loginData);
      return;
    }
    
    const token = loginData.data.token;
    console.log("Login success. Token length:", token.length);

    // 2. Call Navigation
    console.log("Calling Navigation...");
    const origin = '126.6303,45.8034';
    const destination = '126.6310,45.8040';
    const navUrl = `http://localhost:8080/api/client/v1/map/direction/driving?origin=${origin}&destination=${destination}`;
    
    const navRes = await fetch(navUrl, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    const navData = await navRes.json();
    console.log("Navigation Result Code:", navData.code);
    console.log("Navigation Data:", JSON.stringify(navData.data, null, 2));

  } catch (error) {
    console.error("Error:", error);
  }
}

test();
