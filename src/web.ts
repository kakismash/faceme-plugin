import { WebPlugin } from '@capacitor/core';

import type { FaceMePlugin } from './definitions';

export class FaceMeWeb extends WebPlugin implements FaceMePlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log(options.value);
    throw new Error('Method not implemented.');
  }

  async initialize(options: { value: string }): Promise<{ value: string }> {
    console.log(options.value);
    throw new Error('Method not implemented.');
  }

  async register(): Promise<void> {
    throw new Error('Method not implemented.');
  }

  /*async detectBitmap(options: { presentationMs: number,  bitmap: string}): Promise<{ value: string }> {
    console.log(options.presentationMs, options.bitmap);
    throw new Error('Method not implemented.');
  }*/
}
