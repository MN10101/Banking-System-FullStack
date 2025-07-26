import React, { useState } from "react";
import axios from "axios";
import Navbar from "./Navbar";
import styles from '../styles/Contact.module.css'; 

const Contact = () => {
  const [email, setEmail] = useState("");
  const [message, setMessage] = useState("");
  const [responseMessage, setResponseMessage] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post("http://localhost:8080/api/contact", { email, message });
      setResponseMessage(response.data || "Message sent successfully!");
      setEmail("");
      setMessage(""); 
    } catch (error) {
      const errorMsg =
        error.response?.data?.message || "Error sending message.";
      setResponseMessage(errorMsg);
    }
  };

  return (
    <div className={styles.contactPage}>
      <Navbar />
      <div className={styles.contactContainer}>
        <h2>Contact Us</h2>
        <form onSubmit={handleSubmit} className={styles.contactForm}>
          <div className={styles.inputContainer}>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Your Email"
              required
            />
          </div>
          
          <div className={styles.inputContainer}>
            <textarea
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              placeholder="Your Message"
              required
            />
          </div>

          <button type="submit" className={styles.submitButton}>Send</button>
        </form>

        {responseMessage && (
          <p className={responseMessage.includes("Error") ? styles.errorMessage : styles.successMessage}>
            {responseMessage}
          </p>
        )}
      </div>
    </div>
  );
};

export default Contact;
