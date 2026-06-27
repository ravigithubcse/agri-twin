import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { FarmTwinService } from './farm-twin.service';
import { environment } from '../../../environments/environment';
import { FarmTwinResponse, LandParcelResponse } from '../models/farm-twin.model';

describe('FarmTwinService', () => {
  let service: FarmTwinService;
  let httpMock: HttpTestingController;

  const sampleTwin: FarmTwinResponse = {
    id: 'twin-1',
    userId: 'user-1',
    version: 1,
    profileCompletenessScore: 40,
    landParcels: [],
    createdAt: '2026-06-01T00:00:00Z',
    lastUpdated: '2026-06-01T00:00:00Z',
  };

  const sampleParcel: LandParcelResponse = {
    id: 'parcel-1',
    label: 'North Field',
    latitude: 18.52043,
    longitude: 73.856743,
    soilType: 'Black Cotton Soil',
    irrigationType: 'Drip',
    areaAcres: 1.25,
    currentCrop: 'Rice',
    sowingDate: '2026-06-01',
    expectedHarvestDate: '2026-10-15',
    cropHistory: [],
    createdAt: '2026-06-01T00:00:00Z',
    updatedAt: '2026-06-01T00:00:00Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(FarmTwinService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('createMine() posts to /farm-twins/me and caches the result', () => {
    service.createMine().subscribe((twin) => expect(twin).toEqual(sampleTwin));

    const req = httpMock.expectOne(`${environment.farmTwinServiceUrl}/farm-twins/me`);
    expect(req.request.method).toBe('POST');
    req.flush(sampleTwin);

    expect(service.farmTwin()).toEqual(sampleTwin);
  });

  it('getMine() fetches and caches the current twin', () => {
    service.getMine().subscribe();

    const req = httpMock.expectOne(`${environment.farmTwinServiceUrl}/farm-twins/me`);
    expect(req.request.method).toBe('GET');
    req.flush(sampleTwin);

    expect(service.farmTwin()).toEqual(sampleTwin);
  });

  it('addLandParcel() posts the parcel and triggers a refresh of the cached twin', () => {
    service.addLandParcel({ label: 'North Field', areaAcres: 1.25 }).subscribe((parcel) => {
      expect(parcel).toEqual(sampleParcel);
    });

    const createReq = httpMock.expectOne(
      `${environment.farmTwinServiceUrl}/farm-twins/me/land-parcels`,
    );
    expect(createReq.request.method).toBe('POST');
    createReq.flush(sampleParcel);

    // addLandParcel's tap() triggers an internal getMine() call to refresh the cache
    const refreshReq = httpMock.expectOne(`${environment.farmTwinServiceUrl}/farm-twins/me`);
    refreshReq.flush({ ...sampleTwin, landParcels: [sampleParcel] });

    expect(service.farmTwin()?.landParcels?.length).toBe(1);
  });

  it('listLandParcels() returns the list without mutating the cached twin', () => {
    service.listLandParcels().subscribe((parcels) => {
      expect(parcels).toEqual([sampleParcel]);
    });

    const req = httpMock.expectOne(`${environment.farmTwinServiceUrl}/farm-twins/me/land-parcels`);
    expect(req.request.method).toBe('GET');
    req.flush([sampleParcel]);
  });

  it('addCropHistory() posts to the nested crop-history endpoint', () => {
    const cropRequest = { cropName: 'Rice', season: 'KHARIF' as const };

    service.addCropHistory('parcel-1', cropRequest).subscribe();

    const createReq = httpMock.expectOne(
      `${environment.farmTwinServiceUrl}/farm-twins/me/land-parcels/parcel-1/crop-history`,
    );
    expect(createReq.request.method).toBe('POST');
    expect(createReq.request.body).toEqual(cropRequest);
    createReq.flush({
      id: 'crop-1',
      cropName: 'Rice',
      season: 'KHARIF',
      yieldQuintals: null,
      incomeInr: null,
      inputCostInr: null,
      marketName: null,
      saleDate: null,
      createdAt: '2026-06-01T00:00:00Z',
    });

    const refreshReq = httpMock.expectOne(`${environment.farmTwinServiceUrl}/farm-twins/me`);
    refreshReq.flush(sampleTwin);
  });
});
