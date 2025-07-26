import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Navbar from './Navbar';
import styles from '../styles/ConvertCurrency.module.css';

const ConvertCurrency = () => {
  const user = JSON.parse(localStorage.getItem("user")); 
  const [fromCurrency, setFromCurrency] = useState(user.currency || 'EUR'); 
  const [toCurrency, setToCurrency] = useState('USD');
  const [amount, setAmount] = useState(user.balance || ''); 
  const [convertedAmount, setConvertedAmount] = useState(null);

  const currencies = [
    'USD', 'EUR', 'GBP', 'AUD', 'CAD', 'JPY', 'CHF', 'INR', 'CNY', 'MXN'
  ];

  const handleConvert = async (e) => {
    e.preventDefault();
    if (!amount || isNaN(amount) || amount <= 0) {
      alert("Please enter a valid amount");
      return;
    }

    try {
      const response = await axios.get(`http://localhost:8080/api/currency/convert?from=${fromCurrency}&to=${toCurrency}&amount=${amount}`);
      if (response.data && response.data.convertedAmount !== undefined) {
        setConvertedAmount(response.data.convertedAmount);
      } else {
        throw new Error("Converted amount not found in response");
      }
    } catch (error) {
      alert("Error converting currency: " + (error.response?.data || error.message));
    }
  };

  // Format amount with thousands separator
  const formattedAmount = new Intl.NumberFormat().format(amount);

  // Format converted amount with thousands separator if it exists
  const formattedConvertedAmount = convertedAmount !== null
    ? new Intl.NumberFormat().format(convertedAmount.toFixed(2))
    : null;

  return (
    <div className={styles.convertCurrencyPage}>
      <Navbar />
      <div className={styles.convertCurrencyContainer}>
        <h2>Convert Currency</h2>
        <form onSubmit={handleConvert} className={styles.convertCurrencyForm}>
          <div className={styles.inputContainer}>
            <label htmlFor="fromCurrency">From Currency</label>
            <select
              id="fromCurrency"
              value={fromCurrency}
              onChange={(e) => setFromCurrency(e.target.value)}
            >
              {currencies.map((currency) => (
                <option key={currency} value={currency}>
                  {currency}
                </option>
              ))}
            </select>
          </div>

          <div className={styles.inputContainer}>
            <label htmlFor="toCurrency">To Currency</label>
            <select
              id="toCurrency"
              value={toCurrency}
              onChange={(e) => setToCurrency(e.target.value)}
            >
              {currencies.map((currency) => (
                <option key={currency} value={currency}>
                  {currency}
                </option>
              ))}
            </select>
          </div>

          <div className={styles.inputContainer}>
            <label htmlFor="amount">Amount</label>
            <input
              type="number"
              id="amount"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              placeholder="Amount"
            />
          </div>

          <button type="submit" className={styles.submitButton}>Convert</button>
        </form>

        {formattedConvertedAmount !== null && (
          <p className={styles.convertedAmountText}>
            Converted Amount: {formattedConvertedAmount} {toCurrency}
          </p>
        )}
      </div>
    </div>
  );
};

export default ConvertCurrency;
