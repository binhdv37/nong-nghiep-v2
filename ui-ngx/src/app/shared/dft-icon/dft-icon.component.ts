// tslint:disable
import { Input, Component, OnInit } from '@angular/core';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';

@Component({
    selector: '<dft-label-icon>',
    template: `<mat-icon  [svgIcon] = 'name'></mat-icon>`
})
export class IconDFTComponent implements OnInit {
    @Input() name: string;
    constructor(
        private matIconRegistry: MatIconRegistry,
        private domSanitizer: DomSanitizer
       ) {
          
      }
    ngOnInit(): void {
        this.matIconRegistry.addSvgIcon(
          this.name,
          this.domSanitizer.bypassSecurityTrustResourceUrl('../../../assets/' + this.name +'.svg')
        );
    }
}
