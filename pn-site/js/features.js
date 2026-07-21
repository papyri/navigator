// Loads random feature for hero section on homepage

document.addEventListener('DOMContentLoaded', function() {
  const features = [

		{
			image: 'features/feature1.jpg',
			alt: 'Papyrus',
			content: "<p>Image: &nbsp; <a href='/current/20701'>HGV: P.Oxy. 1 39</a> &mdash; (April 24th, 52 CE) The weaver Tryphon is released from military service following a medical examination in Alexandria which certified that he was suffering from cataracts and impaired vision.</p>", 
		},

		{
			image: 'features/feature2.jpg',
			alt: 'Papyrus',
			content: "<p>Image: &nbsp; <a href='/current/22035'>HGV: P.Oxy. 16 1903</a> &mdash; (March 10th, 561 CE) The pork-butcher Takous delivers 960lbs of meat to 30 bucellarii (escort troops or, literally, 'biscuit-eaters'). Every bucellarius gets 30lbs each, while Comitas, the possible scribe of the receipt, gets 60lbs.</p>",
		},

		{
			image: 'features/feature3.jpg',
			alt: 'Papyrus',
			content: "<p>Image: &nbsp; <a href='/current/47283'>HGV: SB 28 17203</a> &mdash; (October 21st, 138 BCE) An official writes to the strategos Euphranôr, requesting that a man accused of illegally misappropriating wine be detained so that he can stand trial.</p>",
		},

		{
			image: 'features/feature4.jpg',
			alt: 'Papyrus',
			content: "<p>Image: &nbsp; <a href='/current/1936'>HGV: P.Mich. 1 36</a> &mdash; (May 7th, 254 BCE) Apollonios writes to Zenon, informing him that the brewer Pais lied to them and is trying to skim an artaba's-worth of beer per day off the top for his own profit. He wants Pais arrested and the brewery accounts checked.</p>",
		},

		{
			image: 'features/feature5.jpg',
			alt: 'Papyrus',
			content: "<p>Image: &nbsp; <a href='/current/7496'>HGV: P.Petr. 2 25 (b)</a> &mdash; (March 20th, 226 BCE) The charioteer Kephalon acknowledges receipt of goods to supply various horses and their drivers, including wine and oil for the care of a disabled horse.</p>",
		},

		{
			image: 'features/feature6.jpg',
			alt: 'Papyrus',
			content: "<p>Image: &nbsp; <a href='/current/5284'>HGV: P.Ryl. 2 65</a> &mdash; (March 1st, 67 BCE) The grave-diggers Petosiris & Paris are fined for breaking a grave-diggers guild contract by carrying off corpses which weren't assigned to them.</p>",
		},

		{
			image: 'features/feature7.jpg',
			alt: 'Papyrus',
			content: "<p>Image: &nbsp; <a href='/current/43916'>HGV: P.Grenf. 2 38</a> &mdash; (April 2nd, 80 BCE) Pasion writes to his father Nikon, asking him not to neglect him but to go to the agora to buy him writing materials such as papyrus rolls, pens, and ink.</p>",
		},

		{
			image: 'features/feature8.jpg',
			alt: 'Papyrus',
			content: "<p>Image: &nbsp; <a href='/current/4438'>HGV: P.Rain.Cent. 49</a> &mdash; (June 27th, 212 BCE) Philammon writes to Ptolemaios, saying he's heard that Ptolemaios has arrested & mistreated the co-holder of the village's beer monopoly. He asks for his release for justice…and so the village doesn't lose beer revenue.</p>",
		},

		{
			image: 'features/feature9.jpg',
			alt: 'Papyrus',
			content: "<p>Image: &nbsp; <a href='/current/381934'>HGV: P.Oxy. 79 5209</a> (February 23rd, 267 CE) The high priest Aurelius Aquila signs a contract with two men representing a wrestler named Demetrius #OnThisDay, agreeing that he will pay Demetrius 3,800 silver drachmae to throw a wrestling match against Aurelius' son Nicantinous.</p>",
		}
  ];

  const randomFeature = features[Math.floor(Math.random() * features.length)];

  document.getElementById('features').innerHTML = `
    <img src="${randomFeature.image}" alt="${randomFeature.alt}" class="hero-image" />

    <div class="hero-actions d-none d-md-block">
      <button type="button" class="btn btn-lg btn-primary pt-4 pb-4 ps-5 pe-5 m-3 shadow-lg" onclick="window.location.href='/search'">Search Papyri</button>
      <button type="button" class="btn btn-lg btn-success pt-4 pb-4 ps-5 pe-5 m-3 shadow-lg" onclick="window.location.href='/editor'">Contribute Content</button>
    </div>

    ${randomFeature.content}
  `;
});