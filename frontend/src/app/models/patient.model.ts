export type Gender = 'MALE' | 'FEMALE' | 'OTHER';

export interface Patient {
  id?: number;
  name: string;
  gender: Gender;
  age: number;
  phoneNumber: string;
  bloodGroup?: string;
  allergies?: string;
  chronicConditions?: string;
  createdAt?: string;
}
