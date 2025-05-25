import { Component, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Voter } from '../../../core/models/voter.model';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-voters-modal',
  templateUrl: './voters-modal.component.html',
  styleUrls: ['./voters-modal.component.scss']
})
export class VotersModalComponent {
  @Input() voters: Voter[] = [];

  constructor(public activeModal: NgbActiveModal) {}

  getVoterImageUrl(voter: Voter): string {
    if (!voter.profilePicture || voter.profilePicture === 'default-avatar.png') {
      return 'assets/images/default-avatar.png';
    }
    return `${environment.uploadsUrl}/${voter.profilePicture}`;
  }
}
