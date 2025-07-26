import axios from "axios";

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080";

export const registerUser = async (user) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/auth/register`, user);
    return response.data;
  } catch (error) {
    throw error.response?.data || "Registration failed.";
  }
};


export const loginUser = async (email, password) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/auth/login`, { email, password });
    console.log("Login response: ", response.data);
    
    // Map API response to desired keys, including IBAN
    const userData = {
      firstName: response.data.first_name,
      lastName: response.data.last_name,
      accountNumber: response.data.accountNumber,
      balance: response.data.balance,
      currency: response.data.currency,
      iban: response.data.iban,
    };

    // Save user data to localStorage
    console.log("Saving user data to localStorage:", userData);
    localStorage.setItem("user", JSON.stringify(userData));
    
    return response.data;
  } catch (error) {
    throw error.response?.data || "Login failed.";
  }
};