import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CropHistoryRequest,
  CropHistoryResponse,
  FarmTwinResponse,
  LandParcelRequest,
  LandParcelResponse,
} from '../models/farm-twin.model';

@Injectable({ providedIn: 'root' })
export class FarmTwinService {
  private readonly baseUrl = `${environment.farmTwinServiceUrl}/farm-twins`;

  /** Cached current twin, kept in sync as a signal so the dashboard can react to changes. */
  private readonly farmTwinSignal = signal<FarmTwinResponse | null>(null);
  readonly farmTwin = this.farmTwinSignal.asReadonly();

  constructor(private readonly http: HttpClient) {}

  createMine(): Observable<FarmTwinResponse> {
    return this.http.post<FarmTwinResponse>(`${this.baseUrl}/me`, {}).pipe(
      tap((twin) => this.farmTwinSignal.set(twin)),
    );
  }

  getMine(): Observable<FarmTwinResponse> {
    return this.http.get<FarmTwinResponse>(`${this.baseUrl}/me`).pipe(
      tap((twin) => this.farmTwinSignal.set(twin)),
    );
  }

  addLandParcel(request: LandParcelRequest): Observable<LandParcelResponse> {
    return this.http.post<LandParcelResponse>(`${this.baseUrl}/me/land-parcels`, request).pipe(
      tap(() => this.getMine().subscribe()), // refresh cached twin so completeness score updates
    );
  }

  listLandParcels(): Observable<LandParcelResponse[]> {
    return this.http.get<LandParcelResponse[]>(`${this.baseUrl}/me/land-parcels`);
  }

  getLandParcel(parcelId: string): Observable<LandParcelResponse> {
    return this.http.get<LandParcelResponse>(`${this.baseUrl}/me/land-parcels/${parcelId}`);
  }

  addCropHistory(parcelId: string, request: CropHistoryRequest): Observable<CropHistoryResponse> {
    return this.http.post<CropHistoryResponse>(
      `${this.baseUrl}/me/land-parcels/${parcelId}/crop-history`,
      request,
    ).pipe(
      tap(() => this.getMine().subscribe()),
    );
  }

  listCropHistory(parcelId: string): Observable<CropHistoryResponse[]> {
    return this.http.get<CropHistoryResponse[]>(
      `${this.baseUrl}/me/land-parcels/${parcelId}/crop-history`,
    );
  }
}
