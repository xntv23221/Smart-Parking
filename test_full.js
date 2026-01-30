// const axios = require('axios'); // Not used, removed


async function test() {
  try {
    // 1. Login
    console.log("Logging in...");
    // Note: Using fetch in node requires node 18+ or a polyfill. The previous successful run used node test_nav.js which had fetch.
    // I will stick to the format that worked (the second Write used fetch).
    // Actually, I'll just use the code that worked in the previous turn, which used fetch.
    // Wait, the environment node version is v24.12.0, so fetch is available.
    
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

    const headers = {
      'Authorization': `Bearer ${token}`
    };

    // 2. Call Navigation
    console.log("\n--- Testing Navigation ---");
    const origin = '126.6303,45.8034';
    const destination = '126.6310,45.8040';
    const navUrl = `http://localhost:8080/api/client/v1/map/direction/driving?origin=${origin}&destination=${destination}`;
    
    const navRes = await fetch(navUrl, { method: 'GET', headers });
    const navData = await navRes.json();
    console.log("Navigation Result:", navData.code, navData.data ? "OK" : "No Data");

    // 3. Call Weather
    console.log("\n--- Testing Weather ---");
    const weatherUrl = `http://localhost:8080/api/client/v1/map/weather?city=230100`; // Harbin
    const weatherRes = await fetch(weatherUrl, { method: 'GET', headers });
    const weatherData = await weatherRes.json();
    console.log("Weather Result:", weatherData.code, weatherData.data ? "OK" : "No Data");
    if (weatherData.data && weatherData.data.lives) {
        console.log("Current Weather:", weatherData.data.lives[0].weather);
    }

    // 4. Call Input Tips
    console.log("\n--- Testing Input Tips ---");
    const tipsUrl = `http://localhost:8080/api/client/v1/map/assistant/inputtips?keywords=Parking`;
    const tipsRes = await fetch(tipsUrl, { method: 'GET', headers });
    const tipsData = await tipsRes.json();
    console.log("Input Tips Result:", tipsData.code, tipsData.data ? "OK" : "No Data");

    // 5. Call Nearby Search
    console.log("\n--- Testing Nearby Search ---");
    const aroundUrl = `http://localhost:8080/api/client/v1/map/place/around?location=${origin}&keywords=停车场&radius=1000`;
    const aroundRes = await fetch(aroundUrl, { method: 'GET', headers });
    const aroundData = await aroundRes.json();
    console.log("Nearby Search Result:", aroundData.code, aroundData.data ? "OK" : "No Data");


  } catch (error) {
    console.error("Error:", error);
  }
}

test();
