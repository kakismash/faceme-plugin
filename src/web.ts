import { WebPlugin } from '@capacitor/core';

import type { FaceMePlugin } from './definitions';

export class FaceMeWeb extends WebPlugin implements FaceMePlugin {

  /**
  * Initializes FaceMe SDK with application context  
  * and license key provided by CyberLink.
  * @param license  A license key provided by CyberLink.
  * @returns        The String value with SDK version
  */
  async initialize(options: { license: string }): Promise<{ version: string }> {
    console.log(options.license);
    throw new Error('Method not implemented.');
  }

  /**
    * For each person, all of his/her faces are treated  
    * as a collection in the database, which contains
    * at least one detected face. 
    * @param name     A UTF-8 encoded string name of face collection.  
    * @param encoded  Expected encoded type.
    * @param data     Expected data to encode.
    * @returns        The number of the collection where the face was added.
    */
  async enroll(options: { imageBase64: string,
                          name: string,
                          data?: string }): Promise<{ collectionId: number }> {

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
                                    name: string }): Promise<{ value: boolean }> {
    console.log(options.collectionId);
    console.log(options.name);
    throw new Error('Method not implemented.');
  }

  async getCollectionName(options: { collectionId: number }): Promise<{ name: string }> {
    console.log(options.collectionId);
    throw new Error('Method not implemented.');
  }

  async setCollectionData(options: {collectionId: number,
                                    data: string }): Promise<{ value: boolean }> {
    console.log(options.collectionId);
    console.log(options.data);
    throw new Error('Method not implemented.');
  }

  async getCollectionData(options: { collectionId: number }): Promise<{ data: string }> {
    console.log(options.collectionId);
    throw new Error('Method not implemented.');
  }

  async deleteCollection(options: { collectionId: number }): Promise<{ value: boolean }> {
    console.log(options.collectionId);
    throw new Error('Method not implemented.');
  }
}
