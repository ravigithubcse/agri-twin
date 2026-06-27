import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
import { FarmTwinService } from '../../../core/services/farm-twin.service';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-dashboard-overview',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.scss',
})
export class OverviewComponent implements OnInit {
  private readonly farmTwinService = inject(FarmTwinService);
  private readonly authService = inject(AuthService);

  readonly isLoading = signal(true);
  readonly hasTwin = signal(false);
  readonly isCreatingTwin = signal(false);

  readonly farmTwin = this.farmTwinService.farmTwin;
  readonly currentUser = this.authService.currentUser;

  readonly parcelCount = signal(0);
  readonly totalAcres = signal(0);

  ngOnInit(): void {
    this.loadTwin();
  }

  private loadTwin(): void {
    this.isLoading.set(true);
    this.farmTwinService.getMine().subscribe({
      next: (twin) => {
        this.hasTwin.set(true);
        this.isLoading.set(false);
        const parcels = twin.landParcels ?? [];
        this.parcelCount.set(parcels.length);
        this.totalAcres.set(parcels.reduce((sum, p) => sum + Number(p.areaAcres), 0));
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        if (err.status === 404) {
          this.hasTwin.set(false);
        }
      },
    });
  }

  createTwin(): void {
    this.isCreatingTwin.set(true);
    this.farmTwinService.createMine().subscribe({
      next: () => {
        this.isCreatingTwin.set(false);
        this.loadTwin();
      },
      error: () => this.isCreatingTwin.set(false),
    });
  }
}
