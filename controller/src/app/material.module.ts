import { NgModule } from "@angular/core";
import { MatTableModule } from '@angular/material/table';

const MODULES = [
  MatTableModule
];

@NgModule({
  imports: MODULES,
  exports: MODULES
})
export class MaterialModule {}
