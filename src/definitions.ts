export interface FaceMePlugin {

  initialize(options: {license: string }): Promise<{ version: string }>;

  enroll(options: { imageBase64: string,
                    name:        string,
                    data?:       string }): Promise<{ collectionId: number }>;

  search(options: { imageBase64: string }): Promise<{ collectionId?: string,
                                                      confidence?:   number,
                                                      name?:         string,
                                                      data?:         string}>;

  setCollectionName(options: { collectionId: number,
                               name:         string }): Promise<{ value: boolean }>;

  getCollectionName(options: { collectionId: number }): Promise<{ name: string }>;

  setCollectionData(options: { collectionId: number,
                               data:         string }): Promise<{ value: boolean }>;

  getCollectionData(options: { collectionId: number }): Promise<{ data: string }>;

  deleteCollection(options: { collectionId: number }): Promise<{ value: boolean }>;

  initCamera(options: { x:             number,
                        y:             number,
                        width:         number,
                        height:        number,
                        paddingBottom: number,
                        position:      string }): Promise<void>;

  closeCamera(): Promise<void>;

  takePicture(options: {  width:   number,
                          height:  number,
                          quality: number }): Promise<void>;

}
