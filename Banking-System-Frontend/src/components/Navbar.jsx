import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { FaShoppingCart } from "react-icons/fa";
import "../styles/Navbar.css";

const Navbar = ({ cart = [] }) => {
  const [menuOpen, setMenuOpen] = useState(false); 
  const [profileOpen, setProfileOpen] = useState(false); 
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem("user");
    navigate("/login");
  };

  const toggleMenu = () => setMenuOpen(!menuOpen);
  const toggleProfile = () => setProfileOpen(!profileOpen); 

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <h1 className="navbar-title">
          <span className="navbar-title-glow">Nexgin</span> Bank
        </h1>


        <button className="hamburger" onClick={toggleMenu}>
          <div />
          <div />
          <div />
        </button>

        <div className={`navbar-links ${menuOpen ? "active" : ""}`}>
          <Link to="/dashboard" className="navbar-item">Dashboard</Link>
          <Link to="/about" className="navbar-item">About Us</Link>
          <Link to="/transfer" className="navbar-item">Send Money</Link>
          <Link to="/convert" className="navbar-item">Convert Currency</Link>
          <Link to="/shopping" className="navbar-item">Online Shopping</Link>
          <Link to="/contact" className="navbar-item">Contact Us</Link>
        </div>


        <div className="navbar-actions">
          <button onClick={handleLogout} className="logout-button">Logout</button>


          <div className="user-profile">
            <button className="profile-button" onClick={toggleProfile}>
              <span className="profile-icon">👤</span> Profile
            </button>
            <div className={`profile-dropdown ${profileOpen ? "active" : ""}`}>
              <Link to="/profile" className="dropdown-item">Profile</Link>
              <Link to="/settings" className="dropdown-item">Settings</Link>
              <Link to="/help" className="dropdown-item">Help</Link>
            </div>
          </div>


          <Link to="/shopping" className="cart-icon">
            <FaShoppingCart size={24} color="white" />
            {cart.length > 0 && (
              <span className="cart-count">{cart.length}</span>
            )}
          </Link>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
