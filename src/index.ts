import { registerPlugin } from '@capacitor/core';

import type { FaceMePlugin } from './definitions';

const FaceMe = registerPlugin<FaceMePlugin>('FaceMe', {
  web: () => import('./web').then(m => new m.FaceMeWeb()),
});

export * from './definitions';
export { FaceMe };
