import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";
import "../styles/AccountVerification.css";

const AccountVerification = () => {
  const { token } = useParams();
  const [message, setMessage] = useState("Verifying your account...");
  const [error, setError] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    // Send verification request to the backend
    axios
      .get(`http://localhost:8080/auth/verify?token=${token}`)
      .then((response) => {
        setMessage(response.data.message || "Your account has been successfully verified!");
      })
      .catch((error) => {
        setError(true);
        setMessage(error.response?.data?.message || "Verification failed. Please try again.");
      });
  }, [token]);

  const handleBackToDashboard = () => {
    navigate("/dashboard");
  };

  return (
    <div className="verification-container">
      <h1>{error ? "Error" : "Success"}</h1>
      <p>{message}</p>
      <button className="back-button" onClick={handleBackToDashboard}>
        Back to Dashboard
      </button>
    </div>
  );
};

export default AccountVerification;
