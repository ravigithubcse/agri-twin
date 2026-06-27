import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/dashboard/dashboard-shell.component').then((m) => m.DashboardShellComponent),
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/dashboard/overview/overview.component').then((m) => m.OverviewComponent),
      },
      {
        path: 'land-parcels',
        loadComponent: () =>
          import('./features/farm-twin/land-parcel-list/land-parcel-list.component').then(
            (m) => m.LandParcelListComponent,
          ),
      },
    ],
  },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: '**', redirectTo: 'dashboard' },
];
