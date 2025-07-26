import React from "react";
import Navbar from './Navbar';
import "../styles/AboutUs.css";
import nexginBankLogo from "../assets/nexgin.webp";

const AboutUs = () => {
  return (
    <div className="about-us-container">
      <Navbar />
      <div className="about-us-content">
        <h2 className="about-us-title">
           <span className="highlight">Nexgin</span> Bank
        </h2>
        <div className="about-us-logo">
          <img src={nexginBankLogo} alt="Nexgin Bank Logo" />
        </div>
        <div className="about-us-message">
          <p>
            At Nexgin Bank, we believe in offering world-class banking services
            that prioritize the security and growth of your financial assets. Our
            team of experts is dedicated to providing personalized solutions for
            individuals and businesses. Whether it's managing personal finances or
            expanding your business, Nexgin Bank is your trusted partner in
            navigating the financial landscape.
          </p>
          <p>
            With cutting-edge technology, strong security protocols, and exceptional
            customer support, we are committed to making banking seamless and
            accessible to all.
          </p>
        </div>
      </div>
    </div>
  );
};

export default AboutUs;