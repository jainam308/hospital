import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BillingService } from '../../services/billing.service';
import { Bill, PaymentVerificationRequest } from '../../models/bill.model';

@Component({
  selector: 'app-billing',
  templateUrl: './billing.component.html',
  styleUrls: ['./billing.component.css']
})
export class BillingComponent implements OnInit {
  bills: Bill[] = [];
  displayedBills: Bill[] = [];
  selectedBillForReceipt?: Bill;

  filterStatus: 'ALL' | 'PENDING' | 'PAID' = 'ALL';
  searchQuery: string = '';
  highlightAppointmentId?: number;

  isLoading: boolean = false;
  isProcessingPayment: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';

  constructor(
    private billingService: BillingService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['appointmentId']) {
        this.highlightAppointmentId = Number(params['appointmentId']);
      }
    });
    this.loadBills();
  }

  loadBills(): void {
    this.isLoading = true;
    this.billingService.getAllBills().subscribe({
      next: (data) => {
        this.bills = data;
        this.applyFilter();
        this.isLoading = false;

        if (this.highlightAppointmentId) {
          const match = this.bills.find(b => b.appointmentId === this.highlightAppointmentId);
          if (match) {
            this.selectedBillForReceipt = match;
          }
        }
      },
      error: () => {
        this.errorMessage = 'Failed to load billing records.';
        this.isLoading = false;
      }
    });
  }

  setFilterStatus(status: 'ALL' | 'PENDING' | 'PAID'): void {
    this.filterStatus = status;
    this.applyFilter();
  }

  applyFilter(): void {
    let result = [...this.bills];

    if (this.filterStatus !== 'ALL') {
      result = result.filter(b => b.paymentStatus === this.filterStatus);
    }

    if (this.searchQuery && this.searchQuery.trim() !== '') {
      const q = this.searchQuery.toLowerCase().trim();
      result = result.filter(b =>
        b.billNumber.toLowerCase().includes(q) ||
        b.patientName.toLowerCase().includes(q) ||
        b.doctorName.toLowerCase().includes(q) ||
        b.patientPhone.includes(q)
      );
    }

    this.displayedBills = result;
  }

  get pendingCount(): number {
    return this.bills.filter(b => b.paymentStatus === 'PENDING').length;
  }

  get paidCount(): number {
    return this.bills.filter(b => b.paymentStatus === 'PAID').length;
  }

  get totalRevenue(): number {
    return this.bills
      .filter(b => b.paymentStatus === 'PAID')
      .reduce((sum, b) => sum + (b.totalAmount || 0), 0);
  }

  payWithRazorpay(bill: Bill): void {
    this.isProcessingPayment = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.billingService.createRazorpayOrder(bill.id).subscribe({
      next: (order) => {
        this.billingService.openRazorpayModal(
          order,
          (paymentId, signature) => {
            // Callback when Razorpay payment is authorized
            const verifyReq: PaymentVerificationRequest = {
              billId: bill.id,
              razorpayOrderId: order.orderId,
              razorpayPaymentId: paymentId,
              razorpaySignature: signature
            };

            this.billingService.verifyPayment(verifyReq).subscribe({
              next: (paidBill) => {
                this.successMessage = `Payment of ₹${paidBill.totalAmount} for Invoice #${paidBill.billNumber} successfully processed via Razorpay!`;
                this.isProcessingPayment = false;
                this.loadBills();
                this.selectedBillForReceipt = paidBill;
              },
              error: (err) => {
                this.errorMessage = err.error?.message || 'Payment verification failed.';
                this.isProcessingPayment = false;
              }
            });
          },
          () => {
            this.isProcessingPayment = false;
          }
        );
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Could not initiate Razorpay payment.';
        this.isProcessingPayment = false;
      }
    });
  }

  payWithCash(bill: Bill): void {
    const confirmed = confirm(`Collect ₹${bill.totalAmount} in CASH from ${bill.patientName} for Invoice #${bill.billNumber}?`);
    if (!confirmed) return;

    this.isProcessingPayment = true;
    this.billingService.processCashPayment(bill.id).subscribe({
      next: (paidBill) => {
        this.successMessage = `Cash payment of ₹${paidBill.totalAmount} recorded successfully for Invoice #${paidBill.billNumber}!`;
        this.isProcessingPayment = false;
        this.loadBills();
        this.selectedBillForReceipt = paidBill;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to record cash payment.';
        this.isProcessingPayment = false;
      }
    });
  }

  viewReceipt(bill: Bill): void {
    this.selectedBillForReceipt = bill;
  }

  closeReceipt(): void {
    this.selectedBillForReceipt = undefined;
  }

  printReceipt(): void {
    window.print();
  }
}
