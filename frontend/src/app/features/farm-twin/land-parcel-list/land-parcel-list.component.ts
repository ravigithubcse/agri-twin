import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FarmTwinService } from '../../../core/services/farm-twin.service';
import { LandParcelResponse } from '../../../core/models/farm-twin.model';
import { LandParcelFormComponent } from '../land-parcel-form/land-parcel-form.component';

@Component({
  selector: 'app-land-parcel-list',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatDialogModule,
  ],
  templateUrl: './land-parcel-list.component.html',
  styleUrl: './land-parcel-list.component.scss',
})
export class LandParcelListComponent implements OnInit {
  readonly isLoading = signal(true);
  readonly parcels = signal<LandParcelResponse[]>([]);

  constructor(
    private readonly farmTwinService: FarmTwinService,
    private readonly dialog: MatDialog,
  ) {}

  ngOnInit(): void {
    this.loadParcels();
  }

  loadParcels(): void {
    this.isLoading.set(true);
    this.farmTwinService.listLandParcels().subscribe({
      next: (parcels) => {
        this.parcels.set(parcels);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  openAddParcelDialog(): void {
    const dialogRef = this.dialog.open(LandParcelFormComponent, {
      width: '480px',
      maxWidth: '90vw',
    });

    dialogRef.afterClosed().subscribe((created) => {
      if (created) {
        this.loadParcels();
      }
    });
  }

  totalAcres(): number {
    return this.parcels().reduce((sum, p) => sum + Number(p.areaAcres), 0);
  }
}
