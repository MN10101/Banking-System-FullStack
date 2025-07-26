import React, { useState, useEffect, useRef } from "react";
import axios from "axios";
import "../styles/Chatbot.css";
import messageAlertSound from "../assets/message-alert.mp3";
import chatbotIcon from "../assets/nexgin.webp"; 

const Chatbot = ({ isLoggedIn, userId }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([]);
  const [userMessage, setUserMessage] = useState("");
  const chatbotMessagesRef = useRef(null);

  // Play sound when the chatbot opens
  useEffect(() => {
    if (isOpen) {
      const audio = new Audio(messageAlertSound);
      audio.play().catch((error) => console.error("Error playing sound:", error));
    }
  }, [isOpen]);

  useEffect(() => {
    if (isLoggedIn) {
      setMessages([{ sender: "bot", text: "Welcome! How can I help you today?" }]);
    } else {
      setMessages([]);
      setIsOpen(false);
    }
  }, [isLoggedIn]);

  // Scroll to the latest message automatically
  useEffect(() => {
    if (chatbotMessagesRef.current) {
      chatbotMessagesRef.current.scrollTop = chatbotMessagesRef.current.scrollHeight;
    }
  }, [messages]);

  const handleUserMessage = (e) => {
    setUserMessage(e.target.value);
  };

  // Handle 'Enter' key press to send the message
  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      sendMessage();
    }
  };

  const sendMessage = async () => {
    if (!userMessage.trim()) return;

    setMessages((prevMessages) => [...prevMessages, { sender: "user", text: userMessage }]);

    try {
      console.log("Sending message to backend:", userMessage);
      const response = await axios.post(
        `http://localhost:8080/chatbot/ask?userId=${userId}`,
        userMessage
      );
      console.log("Response from server:", response.data);

      setMessages((prevMessages) => [
        ...prevMessages,
        { sender: "bot", text: response.data },
      ]);
    } catch (error) {
      console.error("Error sending message to chatbot:", error);
    }
    setUserMessage("");
  };

  if (!isLoggedIn) return null;

  return (
    <>
      <div
        className={`chatbot-container ${isOpen ? "open" : ""}`}
        // style={{
        //   backgroundImage: "url('/images/nexgin.webp')",
        //   backgroundSize: "cover",
        // }}
      >
        <div className="chatbot-header">
          <h2>Chatbot</h2>
          <button className="close-btn" onClick={() => setIsOpen(false)}>
            ×
          </button>
        </div>
        <div className="chatbot-messages" ref={chatbotMessagesRef}>
          {messages.map((message, index) => (
            <div key={index} className={`message ${message.sender}`}>
              <span>{message.text}</span>
            </div>
          ))}
        </div>
        <div className="chatbot-input">
          <input
            type="text"
            value={userMessage}
            onChange={handleUserMessage}
            onKeyDown={handleKeyPress}
            placeholder="Ask something..."
          />
          <button onClick={sendMessage}>Send</button>
        </div>
      </div>
      <button
        className={`chatbot-btn ${isOpen ? "open" : ""}`}
        onClick={() => setIsOpen((prev) => !prev)}
        style={{ 
          backgroundImage: `url(${chatbotIcon})`,
          backgroundSize: 'cover',
          backgroundPosition: 'center',
          width: '60px',      
          height: '60px',      
          borderRadius: '50%', 
          border: 'none',
          cursor: 'pointer',
          position: 'fixed',
          bottom: '20px',
          right: '20px',
          boxShadow: '0 2px 5px rgba(0,0,0,0.2)'
        }}
      >
      </button>
    </>
  );
};

export default Chatbot;
