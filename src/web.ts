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

  async enroll(options: { imageBase64: string,
                          name: string,
                          data?: string }): Promise<{ collectionId: string }> {
    console.log(options.imageBase64);
    throw new Error('Method not implemented.');
  }

  async search(options: { imageBase64: string }): Promise<{ collectionId?: string,
                                                            confidence?: number,
                                                            name?: string,
                                                            data?: string}> {
    console.log(options.imageBase64);
    throw new Error('Method not implemented.');
  }

  async setCollectionName(options: {collectionId: number,
                                    name: string }): Promise<{ value: string }> {
    console.log(options.collectionId);
    console.log(options.name);
    throw new Error('Method not implemented.');
  }

  async getCollectionName(options: { collectionId: number }): Promise<{ name: string }> {
    console.log(options.collectionId);
    throw new Error('Method not implemented.');
  }

  async setCollectionData(options: {collectionId: number,
                                    data: string }): Promise<{ value: string }> {
    console.log(options.collectionId);
    console.log(options.data);
    throw new Error('Method not implemented.');
  }

  async getCollectionData(options: { collectionId: number }): Promise<{ data: string }> {
    console.log(options.collectionId);
    throw new Error('Method not implemented.');
  }

  async deleteCollection(options: { collectionId: number }): Promise<{ value: string }> {
    console.log(options.collectionId);
    throw new Error('Method not implemented.');
  }
}
