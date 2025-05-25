import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { SharedModule } from '../../shared/shared.module';
import { ProfileComponent } from './profile/profile.component';
import { UserMemesComponent } from './user-memes/user-memes.component';
import { EditMemeModalComponent } from './user-memes/edit-meme-modal.component';

const routes: Routes = [
  {
    path: '',
    component: ProfileComponent
  },
  {
    path: 'memes',
    component: UserMemesComponent
  }
];

@NgModule({
  declarations: [
    ProfileComponent,
    UserMemesComponent,
    EditMemeModalComponent
  ],
  imports: [
    SharedModule,
    RouterModule.forChild(routes)
  ]
})
export class ProfileModule { }
