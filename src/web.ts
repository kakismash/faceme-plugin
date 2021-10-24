import { WebPlugin } from '@capacitor/core';

import type { FaceMePlugin } from './definitions';

export class FaceMeWeb extends WebPlugin implements FaceMePlugin {

  /**
  * Initializes FaceMe SDK with application context  
  * and license key provided by CyberLink.
  * @param license  A license key provided by CyberLink.
  * @returns        The SDK version.
  */
  async initialize(options: { license: string }): Promise<{ version: string }> {

    console.log(options.license);
    throw new Error('Method not implemented.');
  }

  /**
  * For each person, all of his/her faces are treated  
  * as a collection in the database, which contains
  * at least one detected face. 
  * @param name         A UTF-8 encoded string name of face collection.  
  * @param imageBase64  Expected encoded type.
  * @param data         Expected data to encode.
  * @returns            The number of the collection where the face was added.
  */
  async enroll(options: { name: string,
                          imageBase64: string,
                          data?: string }): Promise<{ collectionId: number }> {

    console.log(options.imageBase64);
    throw new Error('Method not implemented.');
  }

  /**
  * Recognizes a face detected in an image or camera frame  
  * that could match someone from the existing database.
  * @param imageBase64  Expected encoded type.
  * @returns            a JSObject with CollectionId where the face is found,
  *                     the confidence score threshold that leads to the 
  *                     conclusion of the comparison results and
  *                     the name of the face stored in the collection.
  */
  async search(options: { imageBase64: string }): Promise<{ collectionId?: string,
                                                            confidence?: number,
                                                            name?: string,
                                                            data?: string}> {
    console.log(options.imageBase64);
    throw new Error('Method not implemented.');
  }

  /**
  * Set the name of the face collection.
  * @param collectionId  A unique identifier that represents a specified face collection.
  * @param name          A UTF-8 encoded string name of the collection.
  * @returns             True when set face collection was named successfully.
  */
  async setCollectionName(options: {collectionId: number,
                                    name: string }): Promise<{ value: boolean }> {
    console.log(options.collectionId);
    console.log(options.name);
    throw new Error('Method not implemented.');
  }

  /**
  * Get collection name from the face collection.
  * @param collectionId  A unique identifier that represents a specified face collection.
  * @returns             Returns a UTF-8 encoded string name object.                   
  */
  async getCollectionName(options: { collectionId: number }): Promise<{ name: string }> {
    console.log(options.collectionId);
    throw new Error('Method not implemented.');
  }

  /**
  * Set custom data block to the face collection.
  * @param collectionId  A unique identifier that represents a specified face collection.
  * @param data          The data block of user data.
  * @returns             Returns true when set face collection custom data was successful.                     
  */
  async setCollectionData(options: {collectionId: number,
                                    data: string }): Promise<{ value: boolean }> {
    console.log(options.collectionId);
    console.log(options.data);
    throw new Error('Method not implemented.');
  }

  /**
  * Get user data block from the face collection.
  * @param collectionId  A unique identifier that represents a specified face collection.
  * @returns             Returns a String of custom data.                      
  */
  async getCollectionData(options: { collectionId: number }): Promise<{ data: string }> {
    console.log(options.collectionId);
    throw new Error('Method not implemented.');
  }

  /**
  * Delete a specified face collection.
  * @param collectionId  A unique identifier that represents a specified face collection.
  * @returns             Returns true when face collection deletion was successful.                     
  */
  async deleteCollection(options: { collectionId: number }): Promise<{ value: boolean }> {
    console.log(options.collectionId);
    throw new Error('Method not implemented.');
  }

  async initCamera(): Promise<void> {
    throw new Error('Method not implemented.');
  }
}
