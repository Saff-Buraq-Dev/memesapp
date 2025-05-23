import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { SharedModule } from '../../shared/shared.module';
import { MemeListComponent } from './meme-list/meme-list.component';
import { MemeDetailComponent } from './meme-detail/meme-detail.component';
import { MemeUploadComponent } from './meme-upload/meme-upload.component';
import { VotersModalComponent } from './voters-modal/voters-modal.component';
import { AuthGuard } from '../../core/guards/auth.guard';

// Angular Material
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { NgbModalModule } from '@ng-bootstrap/ng-bootstrap';

const routes: Routes = [
  {
    path: '',
    component: MemeListComponent
  },
  {
    path: 'upload',
    component: MemeUploadComponent,
    canActivate: [AuthGuard]
  },
  {
    path: ':id',
    component: MemeDetailComponent
  }
];

@NgModule({
  declarations: [
    MemeListComponent,
    MemeDetailComponent,
    MemeUploadComponent,
    VotersModalComponent
  ],
  imports: [
    SharedModule,
    RouterModule.forChild(routes),
    NgbModalModule,
    // Angular Material
    MatChipsModule,
    MatFormFieldModule,
    MatIconModule
  ]
})
export class MemesModule { }
