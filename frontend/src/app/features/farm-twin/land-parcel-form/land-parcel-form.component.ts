import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FarmTwinService } from '../../../core/services/farm-twin.service';
import { ApiErrorResponse } from '../../../core/models/user.model';

@Component({
  selector: 'app-land-parcel-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './land-parcel-form.component.html',
  styleUrl: './land-parcel-form.component.scss',
})
export class LandParcelFormComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly farmTwinService = inject(FarmTwinService);
  private readonly dialogRef = inject(MatDialogRef<LandParcelFormComponent>);

  readonly isSubmitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.formBuilder.group({
    label: ['', [Validators.required]],
    areaAcres: [null as number | null, [Validators.required, Validators.min(0.01)]],
    soilType: [''],
    irrigationType: [''],
    currentCrop: [''],
    sowingDate: [null as Date | null],
    expectedHarvestDate: [null as Date | null],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    const raw = this.form.getRawValue();

    this.farmTwinService.addLandParcel({
      label: raw.label!,
      areaAcres: raw.areaAcres!,
      soilType: raw.soilType || undefined,
      irrigationType: raw.irrigationType || undefined,
      currentCrop: raw.currentCrop || undefined,
      sowingDate: raw.sowingDate ? this.toIsoDate(raw.sowingDate) : undefined,
      expectedHarvestDate: raw.expectedHarvestDate ? this.toIsoDate(raw.expectedHarvestDate) : undefined,
    }).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.dialogRef.close(true);
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        const apiError = err.error as ApiErrorResponse | undefined;
        this.errorMessage.set(apiError?.message ?? 'Could not add land parcel. Please try again.');
      },
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }

  private toIsoDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }
}
