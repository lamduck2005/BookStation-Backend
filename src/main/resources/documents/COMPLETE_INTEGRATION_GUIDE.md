# 🎯 COMPLETE INTEGRATION GUIDE - CHECKOUT & ORDER SYSTEM

## 📌 **OVERVIEW**

Hướng dẫn tích hợp **hoàn chính** hệ thống Checkout Session + Order Creation cho Frontend, bao gồm tất cả API calls, error handling, và best practices.

---

## 🚀 **COMPLETE FLOW DIAGRAM**

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   CART ITEMS    │───▶│ CHECKOUT SESSION │───▶│     ORDER       │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
    Add/Remove Items         Save State              Final Purchase
    Price Calculation       Validate Items           Process Payment
    Flash Sale Detection    Apply Vouchers           Update Inventory
```

---

## 🎯 **STEP-BY-STEP INTEGRATION**

### **STEP 1: Create Session from Cart**

```javascript
// Frontend: User clicks "Proceed to Checkout"
const proceedToCheckout = async (userId) => {
  try {
    const response = await fetch(`/api/checkout-sessions/from-cart?userId=${userId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    });
    
    const result = await response.json();
    
    if (result.status === 201) {
      // Success: Navigate to checkout page với session data
      localStorage.setItem('checkoutSessionId', result.data.id);
      router.push(`/checkout/${result.data.id}`);
      return result.data;
    } else {
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('Error creating checkout session:', error);
    alert('Không thể tạo phiên checkout. Vui lòng thử lại.');
  }
};
```

### **STEP 2: Checkout Page - Load & Display Session**

```javascript
// Frontend: Checkout page component
const CheckoutPage = ({ sessionId }) => {
  const [session, setSession] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  useEffect(() => {
    loadSession();
  }, [sessionId]);
  
  const loadSession = async () => {
    try {
      const response = await fetch(`/api/checkout-sessions/${sessionId}?userId=${userId}`);
      const result = await response.json();
      
      if (result.status === 200) {
        setSession(result.data);
        
        // Check session expiry
        if (result.data.isExpired) {
          handleSessionExpired();
          return;
        }
      } else {
        setError(result.message);
      }
    } catch (error) {
      setError('Không thể tải thông tin checkout');
    } finally {
      setLoading(false);
    }
  };
  
  const handleSessionExpired = () => {
    alert('Phiên checkout đã hết hạn. Tạo phiên mới...');
    proceedToCheckout(userId); // Tạo session mới
  };
  
  return (
    <div className="checkout-page">
      {loading && <LoadingSpinner />}
      {error && <ErrorMessage message={error} />}
      {session && (
        <>
          <AddressSection session={session} onUpdate={updateSession} />
          <ItemsSection items={session.checkoutItems} />
          <VouchersSection session={session} onUpdate={updateSession} />
          <ShippingSection session={session} onUpdate={updateSession} />
          <PaymentSection session={session} onUpdate={updateSession} />
          <OrderSummary session={session} />
          <OrderButton session={session} onOrder={handleCreateOrder} />
        </>
      )}
    </div>
  );
};
```

### **STEP 3: Update Session (Address, Vouchers, etc.)**

```javascript
// Frontend: Update session khi user thay đổi
const updateSession = async (updates) => {
  try {
    const sessionData = {
      userId: session.userId,
      ...updates,
      checkoutItems: session.checkoutItems // Giữ nguyên items
    };
    
    const response = await fetch(`/api/checkout-sessions/${session.id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(sessionData)
    });
    
    const result = await response.json();
    
    if (result.status === 200) {
      setSession(result.data);
      showSuccessToast('Cập nhật thành công');
    } else {
      showErrorToast(result.message);
    }
  } catch (error) {
    showErrorToast('Lỗi khi cập nhật thông tin');
  }
};

// Example: User selects address
const handleAddressSelect = (addressId) => {
  updateSession({
    addressId: addressId,
    shippingFee: calculateShippingFee(addressId),
    estimatedDeliveryFrom: calculateDeliveryTime(addressId).from,
    estimatedDeliveryTo: calculateDeliveryTime(addressId).to
  });
};

// Example: User applies vouchers
const handleVoucherApply = (voucherIds) => {
  updateSession({
    selectedVoucherIds: voucherIds
  });
};
```

### **STEP 4: Validate Before Order**

```javascript
// Frontend: Validate session trước khi cho phép đặt hàng
const validateBeforeOrder = async () => {
  try {
    const response = await fetch(`/api/checkout-sessions/${session.id}/validate?userId=${userId}`, {
      method: 'POST'
    });
    
    const result = await response.json();
    
    if (result.status === 200) {
      return { isValid: true };
    } else {
      return { 
        isValid: false, 
        errors: result.message 
      };
    }
  } catch (error) {
    return { 
      isValid: false, 
      errors: 'Lỗi khi kiểm tra đơn hàng' 
    };
  }
};

// Hiển thị validation errors
const ValidationErrors = ({ errors }) => (
  <div className="validation-errors">
    <h4>⚠️ Có lỗi cần xử lý:</h4>
    <ul>
      {errors.split(';').map((error, index) => (
        <li key={index} className="error-item">{error.trim()}</li>
      ))}
    </ul>
    <button onClick={loadSession}>🔄 Kiểm tra lại</button>
  </div>
);
```

### **STEP 5: Create Order**

```javascript
// Frontend: Tạo đơn hàng từ session
const handleCreateOrder = async () => {
  try {
    setOrderLoading(true);
    
    // 1. Validate session trước
    const validation = await validateBeforeOrder();
    if (!validation.isValid) {
      setValidationErrors(validation.errors);
      return;
    }
    
    // 2. Confirm với user
    const confirmed = await showConfirmDialog({
      title: 'Xác nhận đặt hàng',
      message: `Tổng tiền: ${formatPrice(session.totalAmount)}đ\nBạn có chắc muốn đặt hàng?`
    });
    
    if (!confirmed) return;
    
    // 3. Tạo order
    const response = await fetch(`/api/checkout-sessions/${session.id}/create-order?userId=${userId}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    const result = await response.json();
    
    if (result.status === 201) {
      // Success: Redirect to success page
      localStorage.removeItem('checkoutSessionId');
      router.push(`/order-success/${result.data}`);
      showSuccessToast(result.message);
    } else {
      // Error: Handle specific cases
      handleOrderError(result.message, result.status);
    }
    
  } catch (error) {
    console.error('Order creation error:', error);
    showErrorToast('Lỗi hệ thống. Vui lòng thử lại sau.');
  } finally {
    setOrderLoading(false);
  }
};

// Handle order errors với specific actions
const handleOrderError = (errorMessage, statusCode) => {
  if (statusCode === 400) {
    if (errorMessage.includes('hết hạn')) {
      // Session expired
      showDialog({
        title: '⏰ Phiên hết hạn',
        message: 'Phiên checkout đã hết hạn. Chúng tôi sẽ tạo phiên mới từ giỏ hàng.',
        confirmText: 'Tạo phiên mới',
        onConfirm: () => proceedToCheckout(userId)
      });
    } else if (errorMessage.includes('hết hàng')) {
      // Stock issues
      showDialog({
        title: '📦 Hết hàng',
        message: errorMessage + '\n\nBạn có muốn cập nhật giỏ hàng?',
        confirmText: 'Về giỏ hàng',
        onConfirm: () => router.push('/cart')
      });
    } else if (errorMessage.includes('thay đổi')) {
      // Price changes
      showDialog({
        title: '💰 Giá thay đổi',
        message: errorMessage + '\n\nVui lòng cập nhật lại đơn hàng.',
        confirmText: 'Cập nhật',
        onConfirm: () => {
          loadSession(); // Reload session with new prices
          updateSession({}); // Trigger recalculation
        }
      });
    } else {
      // Generic validation error
      setValidationErrors(errorMessage);
    }
  } else {
    // System errors
    showErrorToast('Lỗi hệ thống. Vui lòng thử lại sau.');
  }
};
```

---

## 🎯 **COMPLETE REACT COMPONENTS**

### **Checkout Session Hook**
```javascript
// hooks/useCheckoutSession.js
export const useCheckoutSession = () => {
  const [session, setSession] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  const createFromCart = async (userId) => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.post(`/checkout-sessions/from-cart?userId=${userId}`);
      const newSession = response.data.data;
      setSession(newSession);
      return newSession;
    } catch (err) {
      setError(err.response?.data?.message || 'Lỗi tạo session');
      throw err;
    } finally {
      setLoading(false);
    }
  };
  
  const loadSession = async (sessionId, userId) => {
    setLoading(true);
    try {
      const response = await api.get(`/checkout-sessions/${sessionId}?userId=${userId}`);
      setSession(response.data.data);
      return response.data.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Lỗi tải session');
      throw err;
    } finally {
      setLoading(false);
    }
  };
  
  const updateSession = async (sessionId, updates) => {
    setLoading(true);
    try {
      const response = await api.put(`/checkout-sessions/${sessionId}`, {
        userId: session.userId,
        ...updates,
        checkoutItems: session.checkoutItems
      });
      setSession(response.data.data);
      return response.data.data;
    } catch (err) {
      setError(err.response?.data?.message || 'Lỗi cập nhật session');
      throw err;
    } finally {
      setLoading(false);
    }
  };
  
  const validateSession = async (sessionId, userId) => {
    try {
      const response = await api.post(`/checkout-sessions/${sessionId}/validate?userId=${userId}`);
      return { isValid: true, data: response.data.data };
    } catch (err) {
      return { 
        isValid: false, 
        message: err.response?.data?.message || 'Validation failed' 
      };
    }
  };
  
  const createOrder = async (sessionId, userId) => {
    setLoading(true);
    try {
      const response = await api.post(`/checkout-sessions/${sessionId}/create-order?userId=${userId}`);
      return response.data.data;
    } catch (err) {
      throw err;
    } finally {
      setLoading(false);
    }
  };
  
  return {
    session,
    loading,
    error,
    createFromCart,
    loadSession,
    updateSession,
    validateSession,
    createOrder
  };
};
```

### **Complete Checkout Page**
```javascript
// pages/CheckoutPage.jsx
import { useCheckoutSession } from '../hooks/useCheckoutSession';

const CheckoutPage = () => {
  const { session, loading, error, loadSession, updateSession, validateSession, createOrder } = useCheckoutSession();
  const [validationErrors, setValidationErrors] = useState(null);
  const [orderLoading, setOrderLoading] = useState(false);
  const { sessionId } = useParams();
  const { userId } = useAuth();
  
  useEffect(() => {
    if (sessionId && userId) {
      loadSession(sessionId, userId);
    }
  }, [sessionId, userId]);
  
  // Auto-validate session every 30 seconds
  useEffect(() => {
    if (!session) return;
    
    const interval = setInterval(async () => {
      const validation = await validateSession(session.id, userId);
      if (!validation.isValid) {
        setValidationErrors(validation.message);
      } else {
        setValidationErrors(null);
      }
    }, 30000);
    
    return () => clearInterval(interval);
  }, [session]);
  
  const handleAddressChange = async (addressId) => {
    await updateSession(session.id, {
      addressId,
      shippingFee: calculateShippingFee(addressId),
      estimatedDeliveryFrom: calculateDeliveryTime(addressId).from,
      estimatedDeliveryTo: calculateDeliveryTime(addressId).to
    });
  };
  
  const handleVoucherChange = async (voucherIds) => {
    await updateSession(session.id, {
      selectedVoucherIds: voucherIds
    });
  };
  
  const handlePaymentMethodChange = async (paymentMethod) => {
    await updateSession(session.id, {
      paymentMethod
    });
  };
  
  const handleCreateOrder = async () => {
    try {
      setOrderLoading(true);
      
      // Final validation
      const validation = await validateSession(session.id, userId);
      if (!validation.isValid) {
        setValidationErrors(validation.message);
        return;
      }
      
      // Create order
      const orderId = await createOrder(session.id, userId);
      
      // Success
      localStorage.removeItem('checkoutSessionId');
      router.push(`/order-success/${orderId}`);
      
    } catch (error) {
      handleOrderError(error.response?.data?.message, error.response?.status);
    } finally {
      setOrderLoading(false);
    }
  };
  
  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorPage message={error} />;
  if (!session) return <NotFoundPage />;
  
  return (
    <div className="checkout-page">
      <div className="checkout-header">
        <h1>Thanh toán</h1>
        <SessionTimer expiresAt={session.expiresAt} onExpired={handleSessionExpired} />
      </div>
      
      {validationErrors && (
        <ValidationErrorsCard 
          errors={validationErrors} 
          onRetry={() => loadSession(session.id, userId)} 
        />
      )}
      
      <div className="checkout-content">
        <div className="checkout-left">
          <AddressSection 
            session={session} 
            onChange={handleAddressChange} 
          />
          
          <ItemsSection 
            items={session.checkoutItems} 
            onRecalculate={() => updateSession(session.id, {})}
          />
          
          <VouchersSection 
            session={session} 
            onChange={handleVoucherChange} 
          />
          
          <ShippingSection 
            session={session} 
            onChange={(method) => updateSession(session.id, { shippingMethod: method })}
          />
          
          <PaymentSection 
            session={session} 
            onChange={handlePaymentMethodChange} 
          />
        </div>
        
        <div className="checkout-right">
          <OrderSummaryCard session={session} />
          
          <OrderButton 
            session={session}
            loading={orderLoading}
            disabled={!!validationErrors || !session.address || !session.paymentMethod}
            onClick={handleCreateOrder}
          />
        </div>
      </div>
    </div>
  );
};
```

---

## 🎯 **ERROR HANDLING PATTERNS**

### **Network Error Handler**
```javascript
const handleApiError = (error, context) => {
  const status = error.response?.status;
  const message = error.response?.data?.message;
  
  switch (status) {
    case 400:
      if (context === 'order-creation') {
        handleOrderError(message, status);
      } else {
        showErrorToast(message);
      }
      break;
      
    case 401:
      // Unauthorized - redirect to login
      router.push('/login');
      break;
      
    case 404:
      if (context === 'session') {
        showDialog({
          title: 'Session không tồn tại',
          message: 'Phiên checkout không tồn tại. Tạo phiên mới?',
          onConfirm: () => proceedToCheckout(userId)
        });
      }
      break;
      
    case 500:
      showErrorToast('Lỗi hệ thống. Vui lòng thử lại sau.');
      break;
      
    default:
      showErrorToast(message || 'Đã có lỗi xảy ra');
  }
};
```

### **Session Expiry Handler**
```javascript
const SessionTimer = ({ expiresAt, onExpired }) => {
  const [timeLeft, setTimeLeft] = useState(0);
  
  useEffect(() => {
    const interval = setInterval(() => {
      const now = Date.now();
      const left = expiresAt - now;
      
      if (left <= 0) {
        onExpired();
        clearInterval(interval);
      } else {
        setTimeLeft(left);
      }
    }, 1000);
    
    return () => clearInterval(interval);
  }, [expiresAt]);
  
  const formatTime = (ms) => {
    const minutes = Math.floor(ms / 60000);
    const seconds = Math.floor((ms % 60000) / 1000);
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  };
  
  return (
    <div className={`session-timer ${timeLeft < 300000 ? 'warning' : ''}`}>
      ⏰ Hết hạn sau: {formatTime(timeLeft)}
    </div>
  );
};
```

---

## 📊 **MONITORING & ANALYTICS**

### **Track User Actions**
```javascript
// Analytics tracking
const trackCheckoutEvent = (event, data) => {
  analytics.track('Checkout Event', {
    event,
    sessionId: data.sessionId,
    userId: data.userId,
    timestamp: Date.now(),
    ...data
  });
};

// Usage examples
trackCheckoutEvent('session_created', { sessionId, totalAmount });
trackCheckoutEvent('address_selected', { sessionId, addressId });
trackCheckoutEvent('voucher_applied', { sessionId, voucherIds });
trackCheckoutEvent('order_created', { sessionId, orderId });
trackCheckoutEvent('order_failed', { sessionId, error });
```

---

*📝 Guide này cung cấp toàn bộ implementation cần thiết cho việc tích hợp Checkout Session + Order system. Update theo các thay đổi API.*
