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

  async enrollingFace(options: { imageBase64: string }): Promise<{ faceId: string }> {
    console.log(options.imageBase64);
    throw new Error('Method not implemented.');
  }

  async searchFace(options: { imageBase64: string }): Promise<{ faceId: string }> {
    console.log(options.imageBase64);
    throw new Error('Method not implemented.');
  }
}
