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

  async changeCollectionName(options: { collectionId: number, name: string }): Promise<{ value: string }> {
    console.log(options.collectionId);
    console.log(options.name);
    throw new Error('Method not implemented.');
  }

  async getCollectionName(options: { collectionId: number }): Promise<{ name: string }> {
    console.log(options.collectionId);
    throw new Error('Method not implemented.');
  }

  async deleteFace(options: { faceId: number }): Promise<{ value: string }> {
    console.log(options.faceId);
    throw new Error('Method not implemented.');
  }

}
