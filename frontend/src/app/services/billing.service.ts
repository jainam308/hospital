import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Bill, PaymentVerificationRequest, RazorpayOrderResponse } from '../models/bill.model';

declare var Razorpay: any;

@Injectable({
  providedIn: 'root'
})
export class BillingService {
  private apiUrl = 'http://localhost:8081/api/billing';

  constructor(private http: HttpClient) {}

  getAllBills(): Observable<Bill[]> {
    return this.http.get<Bill[]>(this.apiUrl);
  }

  getBillByAppointment(appointmentId: number): Observable<Bill> {
    return this.http.get<Bill>(`${this.apiUrl}/appointment/${appointmentId}`);
  }

  generateBill(appointmentId: number): Observable<Bill> {
    return this.http.post<Bill>(`${this.apiUrl}/generate/${appointmentId}`, {});
  }

  createRazorpayOrder(billId: number): Observable<RazorpayOrderResponse> {
    return this.http.post<RazorpayOrderResponse>(`${this.apiUrl}/create-order/${billId}`, {});
  }

  verifyPayment(request: PaymentVerificationRequest): Observable<Bill> {
    return this.http.post<Bill>(`${this.apiUrl}/verify-payment`, request);
  }

  processCashPayment(billId: number): Observable<Bill> {
    return this.http.post<Bill>(`${this.apiUrl}/cash-payment/${billId}`, {});
  }

  getBillsByPatient(patientId: number): Observable<Bill[]> {
    return this.http.get<Bill[]>(`${this.apiUrl}/patient/${patientId}`);
  }

  /**
   * Opens Razorpay standard checkout popup or mock fallback if Razorpay JS SDK is not loaded.
   */
  openRazorpayModal(
    order: RazorpayOrderResponse,
    onSuccess: (paymentId: string, signature: string) => void,
    onDismiss?: () => void
  ): void {
    if (typeof Razorpay !== 'undefined') {
      const options = {
        key: order.keyId,
        amount: Math.round(order.amount * 100),
        currency: order.currency || 'INR',
        name: order.companyName || 'OPD Care Hospital',
        description: `OPD Consultation Invoice #${order.billNumber}`,
        order_id: order.orderId,
        prefill: {
          name: order.patientName,
          contact: order.patientPhone
        },
        theme: {
          color: '#2563eb'
        },
        handler: (response: any) => {
          onSuccess(response.razorpay_payment_id, response.razorpay_signature);
        },
        modal: {
          ondismiss: () => {
            if (onDismiss) onDismiss();
          }
        }
      };

      const rzp = new Razorpay(options);
      rzp.on('payment.failed', (err: any) => {
        alert('Payment failed: ' + (err.error?.description || 'Transaction declined'));
      });
      rzp.open();
    } else {
      // Fallback for offline / demo environments
      const simulatedPaymentId = 'pay_sim_' + Math.random().toString(36).substring(2, 10);
      const simulatedSignature = 'mock_signature_opd_care';
      const confirmed = confirm(
        `[Razorpay Demo Mode]\n\nInvoice: ${order.billNumber}\nAmount: ₹${order.amount}\nOrder ID: ${order.orderId}\n\nClick OK to simulate successful online payment capture.`
      );
      if (confirmed) {
        onSuccess(simulatedPaymentId, simulatedSignature);
      } else if (onDismiss) {
        onDismiss();
      }
    }
  }
}
