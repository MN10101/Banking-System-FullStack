import React from 'react';
import styles from '../styles/ProductCard.module.css';

const ProductCard = ({ product, onAddToCart }) => (
  <div className={styles.productCard}>
    <img src={product.image} alt={product.name} />
    <h3>{product.name}</h3>
    <p>{product.description}</p>
    <p>€{product.price.toFixed(2)}</p>
    <button onClick={() => onAddToCart(product)}>Add to Cart</button>
  </div>
);

export default ProductCard;
