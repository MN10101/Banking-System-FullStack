import React from "react";
import Navbar from "./Navbar";
import "../styles/Dashboard.css";

const Dashboard = ({ isLoggedIn, handleLogout, balance }) => {
  const user = JSON.parse(localStorage.getItem("user"));

  

  const displayBalance = balance !== null ? balance : user.balance;
  const formattedBalance = new Intl.NumberFormat().format(displayBalance);

  return (
    <div className="dashboard-container">
      <Navbar handleLogout={handleLogout} />
      <div className="dashboard-content">
        <h2 className="welcome-message">
          Welcome, <span>{user.firstName} {user.lastName}</span>
        </h2>
        <div className="dashboard-sections">
          <div className="section-title">Account Details</div>
          <div className="balance">
            <span className="balance-label">Balance: </span>
            <span className="balance-amount">{formattedBalance} {user.currency || "EUR"}</span>
          </div>
          <div className="account-info">
            <div><span className="account-label">Account Number:</span> {user.accountNumber}</div>
            <div><span className="account-label">IBAN:</span> {user.iban}</div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;