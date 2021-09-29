import { WebPlugin } from '@capacitor/core';

import type { FaceMePlugin } from './definitions';

export class FaceMeWeb extends WebPlugin implements FaceMePlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log(options.value);
    throw new Error('Method not implemented.');
  }

  async initialize(options: { license: string }): Promise<{ version: string }> {
    console.log(options.license);
    throw new Error('Method not implemented.');
  }

  async enroll(options: { imageBase64: string }): Promise<{ collectionId: string }> {
    console.log(options.imageBase64);
    throw new Error('Method not implemented.');
  }

  async search(options: { imageBase64: string }): Promise<{ collectionId: string }> {
    console.log(options.imageBase64);
    throw new Error('Method not implemented.');
  }
}
