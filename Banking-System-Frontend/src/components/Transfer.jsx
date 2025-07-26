import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Navbar from './Navbar';
import styles from '../styles/Transfer.module.css';

const Transfer = () => {
  const user = JSON.parse(localStorage.getItem('user')); 
  const [iban, setIban] = useState('');
  const [amount, setAmount] = useState(user?.balance || ''); 
  const [responseMessage, setResponseMessage] = useState('');

  useEffect(() => {
    if (!amount && user?.balance) {
      setAmount(user.balance); 
    }
  }, [user?.balance, amount]);

  const handleTransfer = async (e) => {
    e.preventDefault();
  
    // Check for a valid amount
    if (!amount || isNaN(amount) || amount <= 0) {
      setResponseMessage('Please enter a valid amount');
      return;
    }
  
    // Assuming 'user.iban' contains the logged-in user's IBAN
    const fromIban = user?.iban;
  
    try {
      // Send both fromIban and toIban along with the amount and currency (default 'EUR' for example)
      const response = await axios.post('http://localhost:8080/api/transfer', {
        fromIban,
        toIban: iban,
        amount: parseFloat(amount),
        currency: 'EUR',
      });
  
      // If transfer is successful, update the balance
      const newBalance = user.balance - amount; 
      setAmount(newBalance); 
  
      // Update localStorage with the new balance
      user.balance = newBalance;
      localStorage.setItem('user', JSON.stringify(user));
  
      // Clear the recipient IBAN input after transfer
      setIban('');
  
      setResponseMessage(response.data.message || 'Transfer successful');
    } catch (error) {
      setResponseMessage(error.response?.data?.message || 'Error during transfer');
    }
  };
  
  // Format the balance (amount) with thousands separator
  const formattedAmount = new Intl.NumberFormat().format(amount);

  return (
    <div className={styles.transferPage}>
      <Navbar />
      <div className={styles.transferContainer}>
        <h2>Send Money</h2>
        <form onSubmit={handleTransfer} className={styles.transferForm}>
          <div className={styles.inputContainer}>
            <label htmlFor="iban">Recipient IBAN</label>
            <input
              type="text"
              id="iban"
              value={iban}
              onChange={(e) => setIban(e.target.value)}
              placeholder="Recipient IBAN"
              required
            />
          </div>
          <div className={styles.inputContainer}>
            <label htmlFor="amount">Amount</label>
            <input
              type="number"
              id="amount"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              placeholder="Amount"
              required
            />
          </div>
          <div className={styles.balanceFormatted}>
            <p><strong>Amount (formatted):</strong> {formattedAmount}</p>
          </div>
          <button type="submit" className={styles.submitButton}>Transfer</button>
        </form>
        {responseMessage && <p className={styles.responseMessage}>{responseMessage}</p>}
      </div>
    </div>
  );
};

export default Transfer;
