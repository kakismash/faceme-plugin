import { registerPlugin } from '@capacitor/core';

import type { FacemePlugin } from './definitions';

const Faceme = registerPlugin<FacemePlugin>('Faceme', {
  web: () => import('./web').then(m => new m.FacemeWeb()),
});

export * from './definitions';
export { Faceme };
