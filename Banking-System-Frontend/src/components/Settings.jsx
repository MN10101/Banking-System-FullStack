import React, { useState, useEffect } from "react";
import Navbar from "./Navbar";
import "../styles/Settings.css";

const Settings = () => {
  // State variables for notifications and alarms
  const [notifications, setNotifications] = useState(true);
  const [alarms, setAlarms] = useState(true);

  // Load settings from localStorage on page load
  useEffect(() => {
    const savedNotifications = localStorage.getItem("notifications");
    const savedAlarms = localStorage.getItem("alarms");

    // If settings exist in localStorage, load them; otherwise, default to true
    if (savedNotifications !== null) {
      setNotifications(savedNotifications === "true");
    }

    if (savedAlarms !== null) {
      setAlarms(savedAlarms === "true");
    }
  }, []);

  // Handle checkbox changes
  const handleNotificationChange = (e) => {
    setNotifications(e.target.checked);
  };

  const handleAlarmChange = (e) => {
    setAlarms(e.target.checked);
  };

  // Save changes to localStorage
  const handleSaveChanges = () => {
    localStorage.setItem("notifications", notifications);
    localStorage.setItem("alarms", alarms);
    alert("Settings saved successfully!");
  };

  return (
    <div className="settings-container">
      <Navbar />
      <div className="settings-content">
        <h2 className="settings-title">Account Settings</h2>
        <div className="settings-section">
          <h3>Notifications</h3>
          <label className="settings-label">
            <input
              type="checkbox"
              checked={notifications}
              onChange={handleNotificationChange}
              className="settings-checkbox"
            />
            Enable email notifications
          </label>
        </div>
        <div className="settings-section">
          <h3>Alarms</h3>
          <label className="settings-label">
            <input
              type="checkbox"
              checked={alarms}
              onChange={handleAlarmChange}
              className="settings-checkbox"
            />
            Enable account activity alarms
          </label>
        </div>
        <div className="settings-actions">
          <button onClick={handleSaveChanges} className="save-button">Save Changes</button>
        </div>
      </div>
    </div>
  );
};

export default Settings;
