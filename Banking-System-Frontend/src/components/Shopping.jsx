import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import Navbar from './Navbar';
import ProductCard from './ProductCard';
import styles from '../styles/Shopping.module.css';
import laptopImage from '../assets/laptop.png';
import phoneImage from '../assets/phone.jpeg';
import tshirtImage from '../assets/tshirt.jpg';
import jeansImage from '../assets/jeans.webp';
import jacketImage from '../assets/jacket.jpg';
import blenderImage from '../assets/blender.jpg';
import vacuumImage from '../assets/vacuum.jpg';
import lampImage from '../assets/lamp.webp';
import coffeemakerImage from '../assets/coffeemaker.webp';
import microwaveImage from '../assets/microwave.webp';

const Shopping = () => {
  const [user, setUser] = useState(JSON.parse(localStorage.getItem('user')));
  const [paymentMethod, setPaymentMethod] = useState('balance');
  const [responseMessage, setResponseMessage] = useState('');
  const [cardDetails, setCardDetails] = useState({
    cardNumber: '',
    expMonth: '',
    expYear: '',
    cvc: ''
  });
  const [cardType, setCardType] = useState('');
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState([]);
  const [showPopup, setShowPopup] = useState(false); 

  const navigate = useNavigate(); 

  useEffect(() => {
    setProducts([
      { id: 1, name: 'Laptop', image: laptopImage, description: 'High-performance laptop', price: 999.99 },
      { id: 2, name: 'Smartphone', image: phoneImage, description: 'Latest smartphone', price: 699.99 },
      { id: 3, name: 'T-shirt', image: tshirtImage, description: 'Comfortable cotton T-shirt', price: 19.99 },
      { id: 4, name: 'Jeans', image: jeansImage, description: 'Stylish blue jeans', price: 49.99 },
      { id: 5, name: 'Jacket', image: jacketImage, description: 'Warm winter jacket', price: 89.99 },
      { id: 6, name: 'Blender', image: blenderImage, description: 'High-speed blender for smoothies', price: 129.99 },
      { id: 7, name: 'Vacuum Cleaner', image: vacuumImage, description: 'Powerful vacuum cleaner for home cleaning', price: 199.99 },
      { id: 8, name: 'Lamp', image: lampImage, description: 'LED desk lamp with adjustable brightness', price: 39.99 },
      { id: 9, name: 'Coffee Maker', image: coffeemakerImage, description: 'Automatic coffee maker with programmable features', price: 79.99 },
      { id: 10, name: 'Microwave Oven', image: microwaveImage, description: 'Compact microwave for quick meals', price: 99.99 },
    ]);
  }, []);

  const handleAddToCart = (product) => {
    setCart([...cart, product]);
  };

  const getTotalAmount = () => {
    return cart.reduce((total, item) => total + item.price, 0);
  };

  const handlePurchase = async (e) => {
    e.preventDefault();
    const totalAmount = getTotalAmount();
  
    // Check if user has enough balance
    if (paymentMethod === 'balance' && totalAmount > user?.balance) {
      setResponseMessage('Insufficient balance');
      return;
    }
  
    const payload = {
      accountNumber: user.accountNumber,
      amount: totalAmount,
      paymentMethod,
      ...(paymentMethod === 'credit-card' && cardDetails),
    };
  
    try {
      const response = await axios.post('http://localhost:8080/api/shopping/purchase', payload, {
          headers: { 'Content-Type': 'application/json' },
      });
  
      setResponseMessage(response.data.message || 'Purchase successful');
  
      if (response.data.success) { 
          // Update user balance in localStorage
          const updatedUser = { ...user, balance: user.balance - totalAmount };
          localStorage.setItem('user', JSON.stringify(updatedUser));
          setUser(updatedUser);
  
          // Reset state after purchase
          setCart([]);
          setCardDetails({
            cardNumber: '',
            expMonth: '',
            expYear: '',
            cvc: '',
          });
          setPaymentMethod('balance');
  
          // Show the success popup for a few seconds
          setShowPopup(true);
  
          // Redirect to dashboard after 2 seconds
          setTimeout(() => {
              navigate('/dashboard');
          }, 2000);
      }
  } catch (error) {
      setResponseMessage(error.response?.data?.message || 'Error during purchase');
  }  
  };

  const handleCardNumberChange = (e) => {
    const sanitizedCardNumber = e.target.value.replace(/\D/g, '');
    setCardDetails({ ...cardDetails, cardNumber: sanitizedCardNumber });

    const detectedType = detectCardType(sanitizedCardNumber);
    setCardType(detectedType);
  };

  const detectCardType = (cardNumber) => {
    const visaPattern = /^4/;
    const masterCardPattern = /^35[2-8]/;
    const amexPattern = /^3[47]/;
    const discoverPattern = /^6(?:011|5[0-9]{2})/;
    

    if (visaPattern.test(cardNumber)) return "Visa";
    if (masterCardPattern.test(cardNumber)) return "MasterCard";
    if (amexPattern.test(cardNumber)) return "American Express";
    if (discoverPattern.test(cardNumber)) return "Discover";
    
    return "Unknown";
  };

  return (
    <div className={styles.shoppingPage}>
      <Navbar cart={cart} />
      
      <div className={styles.products}>
        {products.map((product) => (
          <ProductCard key={product.id} product={product} onAddToCart={handleAddToCart} />
        ))}
      </div>

      <div className={styles.shoppingContainer}>
        <h2>Process Payment</h2>
        <form onSubmit={handlePurchase} className={styles.shoppingForm}>
          <div className={styles.inputContainer}>
            <label htmlFor="totalAmount">Total Amount</label>
            <input
              type="text"
              id="totalAmount"
              value={getTotalAmount()}
              disabled
              placeholder="Total amount"
            />
          </div>

          <div className={styles.inputContainer}>
            <label htmlFor="paymentMethod">Payment Method</label>
            <select
              id="paymentMethod"
              value={paymentMethod}
              onChange={(e) => setPaymentMethod(e.target.value)}
            >
              <option value="balance">Use Balance</option>
              <option value="paypal">PayPal</option>
              <option value="credit-card">Credit Card</option>
            </select>
          </div>

          {paymentMethod === 'credit-card' && (
            <>
              <div className={styles.inputContainer}>
                <label htmlFor="cardNumber">Card Number</label>
                <input
                  type="text"
                  id="cardNumber"
                  value={cardDetails.cardNumber}
                  onChange={handleCardNumberChange}
                  placeholder="Enter card number"
                />
              </div>

              <div className={styles.inputContainer}>
                <label htmlFor="expMonth">Expiration Month</label>
                <input
                  type="text"
                  id="expMonth"
                  value={cardDetails.expMonth}
                  onChange={(e) => setCardDetails({ ...cardDetails, expMonth: e.target.value })}
                  placeholder="MM"
                />
              </div>

              <div className={styles.inputContainer}>
                <label htmlFor="expYear">Expiration Year</label>
                <input
                  type="text"
                  id="expYear"
                  value={cardDetails.expYear}
                  onChange={(e) => setCardDetails({ ...cardDetails, expYear: e.target.value })}
                  placeholder="YY"
                />
              </div>

              <div className={styles.inputContainer}>
                <label htmlFor="cvc">CVC</label>
                <input
                  type="text"
                  id="cvc"
                  value={cardDetails.cvc}
                  onChange={(e) => setCardDetails({ ...cardDetails, cvc: e.target.value })}
                  placeholder="CVC"
                />
              </div>
              {cardType && (
                <div className={styles.cardType}>
                  <p>Detected Card Type: {cardType}</p>
                </div>
              )}
            </>
          )}

          <button type="submit" className={styles.submitButton}>Purchase</button>
        </form>

        {responseMessage && (
          <p className={responseMessage.startsWith('Error') ? styles.errorMessage : styles.successMessage}>
            {responseMessage}
          </p>
        )}
        
        {/* Pop-up message for successful purchase */}
        {showPopup && (
          <div className={styles.popupMessage}>
            Purchase Successful!
          </div>
        )}
      </div>
    </div>
  );
};

export default Shopping;
