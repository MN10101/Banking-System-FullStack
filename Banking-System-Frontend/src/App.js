import React, { useState, useEffect, useRef } from "react";
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from "react-router-dom";
import Register from "./components/Register";
import Login from "./components/Login";
import Dashboard from "./components/Dashboard";
import Transfer from "./components/Transfer";
import ConvertCurrency from "./components/ConvertCurrency";
import Shopping from "./components/Shopping";
import Contact from "./components/Contact";
import AccountVerification from "./components/AccountVerification";
import Footer from "./components/Footer";
import WelcomePage from "./components/WelcomePage";
import AboutUs from "./components/AboutUs";
import Profile from "./components/Profile";
import Settings from "./components/Settings";
import Help from "./components/Help";
import Chatbot from "./components/Chatbot";
import "./styles/scrollUpButton.css";

const App = () => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [balance, setBalance] = useState(null);
  const socketRef = useRef(null);

  useEffect(() => {
    // Simulate login state after 1 second for testing
    const timer = setTimeout(() => {
      setIsLoggedIn(true);
      console.log("User logged in for testing.");
    }, 1000);

    return () => clearTimeout(timer); 
  }, []);

  useEffect(() => {
    if (!isLoggedIn) return;

    // Create WebSocket connection if it doesn't exist
    if (!socketRef.current) {
      console.log("Creating WebSocket connection...");
      socketRef.current = new WebSocket("ws://localhost:8080/ws/balance");

      socketRef.current.onopen = () => {
        console.log("WebSocket connection established.");
      };

      socketRef.current.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          if (data.balance !== undefined) {
            const updatedBalance = data.balance;
            console.log("Received balance update:", updatedBalance);
            setBalance(updatedBalance);

            // Update user balance in localStorage
            const user = JSON.parse(localStorage.getItem("user"));
            if (user) {
              const updatedUser = { ...user, balance: updatedBalance };
              localStorage.setItem("user", JSON.stringify(updatedUser));
            }
          }
        } catch (error) {
          console.error("Error parsing WebSocket message:", error);
        }
      };

      socketRef.current.onerror = (error) => {
        console.error("WebSocket error:", error);
        // Attempt to reconnect on error
        setTimeout(() => {
          if (socketRef.current) {
            socketRef.current.close();
            socketRef.current = null;
          }
        }, 5000); 
      };

      socketRef.current.onclose = () => {
        console.log("WebSocket connection closed.");
        // Attempt to reconnect on close
        setTimeout(() => {
          if (socketRef.current) {
            socketRef.current.close();
            socketRef.current = null;
          }
        }, 5000);
      };
    }

    // Cleanup function to close WebSocket on unmount
    return () => {
      if (socketRef.current) {
        console.log("Closing WebSocket connection...");
        socketRef.current.close();
        socketRef.current = null;
      }
    };
  }, [isLoggedIn]); 

  const handleLogout = () => {
    console.log("Logging out, current isLoggedIn:", isLoggedIn);
    setIsLoggedIn(false);
    console.log("After logout, isLoggedIn:", isLoggedIn);
  };

  useEffect(() => {
    // Show/Hide the scroll-up button based on scrolling
    const handleScroll = () => {
      if (document.body.scrollTop > 100 || document.documentElement.scrollTop > 100) {
        document.body.classList.add("scrolled");
      } else {
        document.body.classList.remove("scrolled");
      }
    };

    window.onscroll = handleScroll;

    return () => {
      window.onscroll = null;
    };
  }, []);

  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  return (
    <Router>
      <MainRoutes isLoggedIn={isLoggedIn} handleLogout={handleLogout} balance={balance} />
      {isLoggedIn && <Chatbot key={isLoggedIn.toString()} isLoggedIn={isLoggedIn} />}
      <button id="scrollUpBtn" onClick={scrollToTop}>
        ↑
      </button>
    </Router>
  );
};

const MainRoutes = ({ isLoggedIn, handleLogout, balance }) => {
  const location = useLocation();
  const showFooter = !["/login", "/register", "/welcome"].includes(location.pathname);

  return (
    <>
      <Routes>
        <Route path="/" element={<Navigate to="/login" />} />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/verify/:token" element={<AccountVerification />} />
        <Route path="/welcome" element={<WelcomePage />} />
        <Route path="/dashboard" element={<Dashboard isLoggedIn={isLoggedIn} handleLogout={handleLogout} balance={balance} />} />
        <Route path="/transfer" element={<Transfer isLoggedIn={isLoggedIn} handleLogout={handleLogout} />} />
        <Route path="/convert" element={<ConvertCurrency isLoggedIn={isLoggedIn} handleLogout={handleLogout} />} />
        <Route path="/shopping" element={<Shopping isLoggedIn={isLoggedIn} handleLogout={handleLogout} />} />
        <Route path="/contact" element={<Contact isLoggedIn={isLoggedIn} handleLogout={handleLogout} />} />
        <Route path="/about" element={<AboutUs isLoggedIn={isLoggedIn} handleLogout={handleLogout} />} />
        <Route path="/profile" element={<Profile isLoggedIn={isLoggedIn} handleLogout={handleLogout} />} />
        <Route path="/settings" element={<Settings isLoggedIn={isLoggedIn} handleLogout={handleLogout} />} />
        <Route path="/help" element={<Help isLoggedIn={isLoggedIn} handleLogout={handleLogout} />} />
      </Routes>

      {showFooter && <Footer />}
    </>
  );
};

export default App;