export type DoctorShift = 'MORNING' | 'EVENING' | 'ALL_DAY';

export interface Department {
  id: number;
  name: string;
  code: string;
  description?: string;
}

export interface Doctor {
  id: number;
  name: string;
  departmentId: number;
  departmentName: string;
  specialization: string;
  consultationFee: number;
  roomNumber?: string;
  shift?: DoctorShift;
  maxDailyQuota?: number;
  slotDurationMinutes?: number;
  email?: string;
  phone?: string;
  active: boolean;
}

export interface SlotConflictResponse {
  conflict: boolean;
}
