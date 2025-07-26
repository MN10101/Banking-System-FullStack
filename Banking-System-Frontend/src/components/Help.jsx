import React from "react";
import { Link } from "react-router-dom"; 
import Navbar from "./Navbar";
import "../styles/Help.css";

const Help = () => {
  return (
    <div className="help-container">
      <Navbar />
      <div className="help-content">
        <h2 className="help-title">Help Center</h2>
        <div className="help-section">
          <h3>Contact Us</h3>
          <p>
            If you need any assistance or have questions about your account, feel free to reach out to our customer support team. You can use the "Contact Us" form to get in touch, and we will get back to you as soon as possible.
          </p>
          <p>
            Alternatively, you can also reach us by email at{" "}
            <a href="mailto:nexgin.bank@gmail.com">nexgin.bank@gmail.com</a>.
          </p>
        </div>
        <div className="help-section">
          <h3>Frequently Asked Questions</h3>
          <ul>
            <li>How do I reset my password?</li>
            <li>How can I transfer money internationally?</li>
            <li>What should I do if I notice suspicious activity on my account?</li>
            <li>How do I apply for a loan?</li>
          </ul>
        </div>
        <div className="help-actions">
          <Link to="/contact" className="contact-button">
            Go to Contact Us Form
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Help;
