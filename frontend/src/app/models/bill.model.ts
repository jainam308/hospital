export type PaymentMode = 'CASH' | 'RAZORPAY';
export type PaymentStatus = 'PENDING' | 'PAID' | 'FAILED' | 'REFUNDED';

export interface Bill {
  id: number;
  billNumber: string;
  appointmentId: number;
  patientId: number;
  patientName: string;
  patientPhone: string;
  doctorName: string;
  consultationFee: number;
  taxAmount: number;
  totalAmount: number;
  paymentMode?: PaymentMode;
  paymentStatus: PaymentStatus;
  razorpayOrderId?: string;
  razorpayPaymentId?: string;
  billedAt: string;
  paidAt?: string;
}

export interface RazorpayOrderResponse {
  orderId: string;
  billId: number;
  billNumber: string;
  amount: number;
  currency: string;
  keyId: string;
  patientName: string;
  patientPhone: string;
  companyName: string;
}

export interface PaymentVerificationRequest {
  billId: number;
  razorpayOrderId: string;
  razorpayPaymentId: string;
  razorpaySignature: string;
}
