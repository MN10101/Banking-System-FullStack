import React, { useEffect, useState } from "react";
import Navbar from "./Navbar";
import "../styles/Profile.css";

const Profile = () => {
  const [user, setUser] = useState(null);

  // Function to calculate age based on birthDate
  const calculateAge = (birthDate) => {
    const today = new Date();
    const birthDateObj = new Date(birthDate);
    let age = today.getFullYear() - birthDateObj.getFullYear();
    const month = today.getMonth();
    const day = today.getDate();
    
    if (month < birthDateObj.getMonth() || (month === birthDateObj.getMonth() && day < birthDateObj.getDate())) {
      age--;
    }

    return age;
  };

  useEffect(() => {
    const storedUser = JSON.parse(localStorage.getItem("user"));
    if (storedUser) {
      console.log("Stored user:", storedUser); 
      // Calculate age dynamically
      const age = calculateAge(storedUser.birthDate);
      // Update the user state with the calculated age
      setUser({ ...storedUser, age });
    }
  }, []);

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("en-GB");
  };

  return (
    <div className="profile-container">
      <Navbar />
      {user ? (
        <div className="profile-content">
          <h2 className="profile-title">Profile Details</h2>
          <div className="profile-details">
            <p><strong>First Name:</strong> {user.firstName}</p>
            <p><strong>Last Name:</strong> {user.lastName}</p>
            <p><strong>Email:</strong> {user.email}</p>
            <p><strong>Age:</strong> {user.age}</p>
            <p><strong>Address:</strong> {user.address}</p>
            <p><strong>Phone Number:</strong> {user.phoneNumber}</p>
            <p><strong>Birth Date:</strong> {formatDate(user.birthDate)}</p>
            <p><strong>Tax Number:</strong> {user.taxNumber}</p>
            <p><strong>ID/Passport:</strong> {user.idOrPassport}</p>
          </div>
        </div>
      ) : (
        <div className="profile-message">
          <p>Loading profile...</p>
        </div>
      )}
    </div>
  );
};

export default Profile;
