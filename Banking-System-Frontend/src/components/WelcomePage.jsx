import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/WelcomePage.css";

const WelcomePage = () => {
  const [displayText, setDisplayText] = useState(""); 
  const [showBank, setShowBank] = useState(false); 
  const navigate = useNavigate();

  useEffect(() => {
    const nexginText = "Nexgin"; 
    let index = 0;

    const interval = setInterval(() => {
      if (index < nexginText.length) {
        // Append only valid characters
        setDisplayText((prev) => prev + nexginText.charAt(index)); 
        index += 1;
      } else {
        clearInterval(interval); 
      }
    }, 300); 

    const animationDuration = nexginText.length * 300;
    const showBankTimeout = setTimeout(() => {
      setShowBank(true); 
    }, animationDuration);

    const redirectTimeout = setTimeout(() => {
      navigate("/dashboard"); 
    }, animationDuration + 1000); 

    return () => {
      clearInterval(interval);
      clearTimeout(showBankTimeout);
      clearTimeout(redirectTimeout);
    };
  }, [navigate]);

  return (
    <div className="welcomePage">
      <h1 className="welcomeTitle">
        <span className="nexginText">{displayText}</span>
        {showBank && <span className="bankText">Bank</span>}
      </h1>
    </div>
  );
};

export default WelcomePage;
