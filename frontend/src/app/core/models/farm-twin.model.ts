export type Season = 'KHARIF' | 'RABI' | 'ZAID' | 'PERENNIAL';

export interface CropHistoryResponse {
  id: string;
  cropName: string;
  season: Season;
  yieldQuintals: number | null;
  incomeInr: number | null;
  inputCostInr: number | null;
  marketName: string | null;
  saleDate: string | null;
  createdAt: string;
}

export interface CropHistoryRequest {
  cropName: string;
  season: Season;
  yieldQuintals?: number;
  incomeInr?: number;
  inputCostInr?: number;
  marketName?: string;
  saleDate?: string;
}

export interface LandParcelResponse {
  id: string;
  label: string;
  latitude: number | null;
  longitude: number | null;
  soilType: string | null;
  irrigationType: string | null;
  areaAcres: number;
  currentCrop: string | null;
  sowingDate: string | null;
  expectedHarvestDate: string | null;
  cropHistory: CropHistoryResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface LandParcelRequest {
  label: string;
  latitude?: number;
  longitude?: number;
  soilType?: string;
  irrigationType?: string;
  areaAcres: number;
  currentCrop?: string;
  sowingDate?: string;
  expectedHarvestDate?: string;
}

export interface FarmTwinResponse {
  id: string;
  userId: string;
  version: number;
  profileCompletenessScore: number;
  landParcels: LandParcelResponse[] | null;
  createdAt: string;
  lastUpdated: string;
}
